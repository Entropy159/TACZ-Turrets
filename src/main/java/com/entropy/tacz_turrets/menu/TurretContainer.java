package com.entropy.tacz_turrets.menu;

import com.entropy.tacz_turrets.turret.TurretEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TurretContainer implements Container {
    public static final int GUN_SLOT = 0;

    private final TurretEntity turret;
    private final int ammoSlots;

    public TurretContainer(TurretEntity turret, int ammoSlots) {
        this.turret = turret;
        this.ammoSlots = ammoSlots;
    }

    public TurretEntity getTurret() {
        return turret;
    }

    @Override
    public int getContainerSize() {
        return ammoSlots + 1;
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < getContainerSize(); slot++) {
            if (!getItem(slot).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        if (slot == GUN_SLOT) return turret.getGunStack();
        return turret.getStackInSlot(slot - 1);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack current = getItem(slot);
        if (current.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = current.split(amount);
        setItem(slot, current);
        return taken;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = getItem(slot).copy();
        setItem(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        if (slot == GUN_SLOT) {
            turret.setGunStack(stack);
        } else {
            turret.setStackInSlot(slot - 1, stack);
        }
        setChanged();
    }

    @Override
    public void setChanged() {
        turret.onInventoryChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return turret.isAlive() && turret.distanceTo(player) < 8.0F;
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < getContainerSize(); slot++) {
            setItem(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        if (slot == GUN_SLOT) return turret.isGun(stack);
        return turret.canAcceptAmmo(stack);
    }
}
