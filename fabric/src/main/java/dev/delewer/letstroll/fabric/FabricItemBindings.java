package dev.delewer.letstroll.fabric;

import java.util.List;
import java.util.Optional;

import dev.delewer.letstroll.platform.ItemBindingService;
import dev.delewer.letstroll.platform.PlayerRef;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricItemBindings implements ItemBindingService {

    private static final String KEY = "lt_bound_action";

    @Override
    public boolean bindHeldItem(PlayerRef holder, String actionId) {
        if (!(holder.handle() instanceof ServerPlayerEntity player)) {
            return false;
        }
        ItemStack item = player.getMainHandStack();
        if (item.isEmpty()) {
            return false;
        }
        NbtCompound nbt = item.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        nbt.putString(KEY, actionId);
        item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        item.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                FabricText.toNative(Component.text("LetsTroll: " + actionId, NamedTextColor.LIGHT_PURPLE)))));
        return true;
    }

    @Override
    public boolean unbindHeldItem(PlayerRef holder) {
        if (!(holder.handle() instanceof ServerPlayerEntity player)) {
            return false;
        }
        ItemStack item = player.getMainHandStack();
        if (item.isEmpty()) {
            return false;
        }
        Optional<String> current = read(item);
        if (current.isEmpty()) {
            return false;
        }
        NbtComponent data = item.get(DataComponentTypes.CUSTOM_DATA);
        if (data != null) {
            NbtCompound nbt = data.copyNbt();
            nbt.remove(KEY);
            item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        }
        item.remove(DataComponentTypes.LORE);
        return true;
    }

    @Override
    public Optional<String> heldBinding(PlayerRef holder) {
        if (!(holder.handle() instanceof ServerPlayerEntity player)) {
            return Optional.empty();
        }
        return read(player.getMainHandStack());
    }

    public static Optional<String> read(ItemStack item) {
        if (item == null || item.isEmpty() || item.getItem() == Items.AIR) {
            return Optional.empty();
        }
        NbtComponent data = item.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) {
            return Optional.empty();
        }
        NbtCompound nbt = data.copyNbt();
        return nbt.contains(KEY) ? nbt.getString(KEY) : Optional.empty();
    }
}
