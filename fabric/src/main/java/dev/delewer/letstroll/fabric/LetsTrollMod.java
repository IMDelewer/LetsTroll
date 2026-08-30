package dev.delewer.letstroll.fabric;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import dev.delewer.letstroll.LetsTroll;
import dev.delewer.letstroll.menu.ClickContext;
import dev.delewer.letstroll.menu.ClickKind;
import dev.delewer.letstroll.menu.ScreenRequest;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.StealthOptions;
import dev.delewer.letstroll.player.PlayerAction;
import dev.delewer.letstroll.text.Lang;
import dev.ua.theroer.magicutils.bootstrap.FabricBootstrap;
import dev.ua.theroer.magicutils.commands.CommandRegistry;
import dev.ua.theroer.magicutils.commands.MagicCommand;
import dev.ua.theroer.magicutils.http.MagicHttpClient;
import dev.ua.theroer.magicutils.lang.LanguageManager;
import dev.ua.theroer.magicutils.lang.Messages;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public final class LetsTrollMod implements ModInitializer {

    private final AtomicReference<MinecraftServer> serverRef = new AtomicReference<>();
    private FabricBootstrap.RuntimeResult magic;
    private LetsTroll core;
    private FabricScheduler scheduler;
    private FabricFakePlayers fakePlayers;
    private FabricPing ping;

    @Override
    public void onInitialize() {
        Supplier<MinecraftServer> server = serverRef::get;
        Path dataFolder = FabricLoader.getInstance().getConfigDir().resolve("letstroll");

        magic = FabricBootstrap.forMod("letstroll", server)
                .permissionPrefix("letstroll")
                .enableCommands()
                .enableDiagnostics()
                .translations(languages -> {
                    languages.registerTranslations("en", Lang.english());
                    languages.setFallbackLanguage("en");
                })
                .buildRuntime();

        scheduler = new FabricScheduler(server);
        FabricStealth stealth = new FabricStealth(server);
        FabricEvents events = new FabricEvents(server);
        FabricTextInput input = new FabricTextInput(scheduler);
        FabricMenus menus = new FabricMenus(() -> core);
        FabricSounds sounds = new FabricSounds(() -> core);
        MagicHttpClient http = magic.runtime().manage("skin-http", MagicHttpClient.builder(magic.platform())
                .userAgent("LetsTroll")
                .mapper(new dev.ua.theroer.magicutils.libs.jackson.databind.ObjectMapper()
                        .configure(dev.ua.theroer.magicutils.libs.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false))
                .build());
        fakePlayers = new FabricFakePlayers(server, scheduler, new FabricSkinResolver(http));
        FabricItemBindings itemBindings = new FabricItemBindings();
        ping = new FabricPing(server);

        FabricPlatform platform = new FabricPlatform(server, dataFolder,
                new FabricConfigStore(magic.configManager()), menus, stealth, events, sounds, input,
                fakePlayers, itemBindings, scheduler, ping);

        core = new LetsTroll(platform);
        selectLanguage(magic.languageManager(), core.config().language());
        core.start();

        ServerLifecycleEvents.SERVER_STARTING.register(serverRef::set);
        ServerLifecycleEvents.SERVER_STOPPING.register(instance -> cleanup());
        ServerTickEvents.END_SERVER_TICK.register(instance -> scheduler.tick());

        ServerPlayConnectionEvents.JOIN.register((handler, sender, instance) -> {
            ServerPlayerEntity player = handler.player;
            events.fireJoin(player);
            if (stealth.hidden(player.getUuid())) {
                StealthOptions options = stealth.optionsOf(player.getUuid());
                if (options != null) {
                    stealth.apply(player, options);
                }
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, instance) -> {
            ServerPlayerEntity player = handler.player;
            events.fireQuit(player);
            LetsTroll running = core;
            if (running != null) {
                running.router().forget(player.getUuid());
            }
            ping.forget(player.getUuid());
            menus.forget(player.getUuid());
            input.forget(player.getUuid());
        });

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            if (input.consume(sender.getUuid(), message.getSignedContent())) {
                return false;
            }
            return !muted(stealth, sender.getUuid());
        });

        ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((message, source, params) ->
                source.getPlayer() == null || !muted(stealth, source.getPlayer().getUuid()));

        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) ->
                onInteract(server, player, world.isClient(), hand, entity));

        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            for (MagicCommand command : core.commands()) {
                CommandRegistry.register("letstroll", dispatcher, command);
            }
        });

        magic.logger().success("Loaded %d modules", core.modules().count());
    }

    private boolean muted(FabricStealth stealth, java.util.UUID id) {
        StealthOptions options = stealth.optionsOf(id);
        return options != null && options.muteChat();
    }

    private ActionResult onInteract(Supplier<MinecraftServer> server, PlayerEntity player, boolean client,
                                    Hand hand, net.minecraft.entity.Entity entity) {
        if (client || hand != Hand.MAIN_HAND || core == null) {
            return ActionResult.PASS;
        }
        if (!(player instanceof ServerPlayerEntity holder) || !(entity instanceof ServerPlayerEntity target)) {
            return ActionResult.PASS;
        }
        Optional<String> binding = FabricItemBindings.read(holder.getMainHandStack());
        if (binding.isEmpty()) {
            return ActionResult.PASS;
        }
        PlayerRef holderRef = new FabricPlayerRef(holder, server);
        if (!holderRef.hasPermission(LetsTroll.PERMISSION_USE)) {
            return ActionResult.PASS;
        }
        PlayerAction action = core.playerActions().all().stream()
                .filter(candidate -> candidate.id().equals(binding.get()))
                .findFirst()
                .orElse(null);
        if (action == null) {
            return ActionResult.PASS;
        }
        PlayerRef targetRef = new FabricPlayerRef(target, server);
        ClickContext context = new ClickContext(core, holderRef, ClickKind.RIGHT,
                ScreenRequest.of("bindings"), core.router());
        action.run(context, targetRef);
        return ActionResult.SUCCESS;
    }

    private void selectLanguage(LanguageManager languages, String language) {
        if (languages == null) {
            return;
        }
        languages.setLanguage(language);
        Messages.setLanguageManager(languages);
    }

    private synchronized void cleanup() {
        if (ping != null) {
            ping.clearAll();
        }
        if (fakePlayers != null) {
            fakePlayers.despawnEverything();
        }
        if (core != null) {
            core.stop();
            core = null;
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
        if (magic != null) {
            magic.runtime().close();
            magic = null;
        }
    }
}
