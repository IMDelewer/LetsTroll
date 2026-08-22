package dev.delewer.letstroll.fabric;

import java.util.Collection;
import java.util.List;

import dev.delewer.letstroll.platform.ChainOps;
import dev.delewer.letstroll.platform.PlayerRef;
import dev.delewer.letstroll.platform.Position;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricChain implements ChainOps {

    @Override
    public String inventoryHash(PlayerRef player) {
        ServerPlayerEntity handle = player(player);
        if (handle == null) {
            return "";
        }
        PlayerInventory inventory = handle.getInventory();
        StringBuilder builder = new StringBuilder();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.isEmpty()) {
                builder.append('-');
            } else {
                builder.append(Registries.ITEM.getId(stack.getItem())).append('x').append(stack.getCount());
            }
            builder.append('|');
        }
        builder.append('#').append(inventory.getSelectedSlot());
        return builder.toString();
    }

    @Override
    public void copyInventory(PlayerRef from, PlayerRef to) {
        ServerPlayerEntity source = player(from);
        ServerPlayerEntity target = player(to);
        if (source == null || target == null) {
            return;
        }
        PlayerInventory sourceInventory = source.getInventory();
        PlayerInventory targetInventory = target.getInventory();
        int size = Math.min(sourceInventory.size(), targetInventory.size());
        for (int slot = 0; slot < size; slot++) {
            targetInventory.setStack(slot, sourceInventory.getStack(slot).copy());
        }
        targetInventory.setSelectedSlot(sourceInventory.getSelectedSlot());
        target.playerScreenHandler.sendContentUpdates();
    }

    @Override
    public void setHealth(PlayerRef player, double health) {
        ServerPlayerEntity handle = player(player);
        if (handle != null) {
            handle.setHealth((float) Math.max(0.0, Math.min(handle.getMaxHealth(), health)));
        }
    }

    @Override
    public int food(PlayerRef player) {
        ServerPlayerEntity handle = player(player);
        return handle == null ? 20 : handle.getHungerManager().getFoodLevel();
    }

    @Override
    public void setFood(PlayerRef player, int food) {
        ServerPlayerEntity handle = player(player);
        if (handle != null) {
            int clamped = Math.max(0, Math.min(20, food));
            handle.getHungerManager().setFoodLevel(clamped);
            handle.getHungerManager().setSaturationLevel(Math.min(clamped, 5f));
        }
    }

    @Override
    public void copyPotions(PlayerRef from, PlayerRef to) {
        ServerPlayerEntity source = player(from);
        ServerPlayerEntity target = player(to);
        if (source == null || target == null) {
            return;
        }
        target.clearStatusEffects();
        for (StatusEffectInstance effect : source.getStatusEffects()) {
            target.addStatusEffect(new StatusEffectInstance(effect));
        }
    }

    @Override
    public void kill(PlayerRef player) {
        ServerPlayerEntity handle = player(player);
        if (handle != null) {
            handle.setHealth(0f);
        }
    }

    @Override
    public void drawChain(Collection<PlayerRef> viewers, List<Position> points) {
        for (PlayerRef viewer : viewers) {
            ServerPlayerEntity handle = player(viewer);
            if (handle == null) {
                continue;
            }
            for (Position point : points) {
                handle.getEntityWorld().spawnParticles(handle, ParticleTypes.CRIT, true, false,
                        point.x(), point.y(), point.z(), 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    private ServerPlayerEntity player(PlayerRef ref) {
        return ref != null && ref.handle() instanceof ServerPlayerEntity handle ? handle : null;
    }
}
