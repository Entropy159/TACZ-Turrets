package com.entropy.tacz_turrets.menu;

import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import com.entropy.tacz_turrets.registry.MenuRegistry;
import com.entropy.tacz_turrets.turret.PlayerTargeting;
import com.entropy.tacz_turrets.turret.TurretEnableType;
import com.entropy.tacz_turrets.turret.TurretEntity;
import com.entropy.tacz_turrets.turret.TurretMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TurretMenu extends AbstractContainerMenu {
    public static final int DATA_SIZE = 10;
    public static final int DATA_HEALTH = 0;
    public static final int DATA_MAX_HEALTH = 1;
    public static final int DATA_ENERGY_LOW = 2;
    public static final int DATA_ENERGY_HIGH = 3;
    public static final int DATA_MAX_ENERGY_LOW = 4;
    public static final int DATA_MAX_ENERGY_HIGH = 5;
    public static final int DATA_ENABLE_TYPE = 6;
    public static final int DATA_MODE = 7;
    public static final int DATA_USES_ENERGY = 8;
    public static final int DATA_PLAYER_TARGETING = 9;

    public static final int BUTTON_ENABLE_TYPE = 0;
    public static final int BUTTON_MODE = 1;
    public static final int BUTTON_PLAYER_TARGETING = 2;

    private final Container container;
    private final ContainerData data;
    private final TurretLayout layout;
    private final @Nullable TurretEntity turret;
    private final String ownerName;
    private final Set<UUID> allies;
    private final boolean canModify;

    public TurretMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, clientTurret(playerInventory, buf), new TurretLayout(buf.readByte(), buf.readByte()), buf.readUtf(), readAllies(buf), buf.readBoolean());
    }

    public TurretMenu(int containerId, Inventory playerInventory, @Nullable TurretEntity turret, TurretLayout layout, String ownerName, Set<UUID> allies, boolean canModify) {
        super(MenuRegistry.TURRET.get(), containerId);
        this.turret = turret;
        this.ownerName = ownerName;
        this.canModify = canModify;
        this.allies = new HashSet<>(allies);
        this.layout = layout;
        this.container = turret == null ? new SimpleContainer(layout.ammoSlots + 1) : new TurretContainer(turret, layout.ammoSlots);
        this.data = turret == null || playerInventory.player.level().isClientSide() ? new SimpleContainerData(DATA_SIZE) : new TurretMenuData(turret);

        addSlot(new Slot(container, TurretContainer.GUN_SLOT, layout.gunSlotX, layout.gunSlotY) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return canModify && container.canPlaceItem(TurretContainer.GUN_SLOT, stack);
            }

            @Override
            public boolean mayPickup(@NotNull Player player) {
                return canModify;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        for (int slot = 0; slot < layout.ammoSlots; slot++) {
            int index = slot + 1;
            addSlot(new Slot(container, index, layout.ammoSlotX(slot), layout.ammoSlotY(slot)) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return canModify && container.canPlaceItem(index, stack);
                }

                @Override
                public boolean mayPickup(@NotNull Player player) {
                    return canModify;
                }
            });
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, layout.playerInventoryX + column * TurretLayout.SLOT, layout.playerInventoryY + row * TurretLayout.SLOT));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, layout.playerInventoryX + column * TurretLayout.SLOT, layout.hotbarY));
        }

        addDataSlots(data);
    }

    @Nullable
    private static TurretEntity clientTurret(Inventory playerInventory, FriendlyByteBuf buf) {
        return playerInventory.player.level().getEntity(buf.readVarInt()) instanceof TurretEntity found ? found : null;
    }

    public TurretLayout getLayout() {
        return layout;
    }

    @Nullable
    public TurretEntity getTurret() {
        return turret;
    }

    public int getHealth() {
        return data.get(DATA_HEALTH);
    }

    public int getMaxHealth() {
        return Math.max(1, data.get(DATA_MAX_HEALTH));
    }

    public int getEnergy() {
        return join(data.get(DATA_ENERGY_LOW), data.get(DATA_ENERGY_HIGH));
    }

    public int getMaxEnergy() {
        return Math.max(1, join(data.get(DATA_MAX_ENERGY_LOW), data.get(DATA_MAX_ENERGY_HIGH)));
    }

    private static int join(int low, int high) {
        return (low & 0xFFFF) | ((high & 0xFFFF) << 16);
    }

    public boolean usesEnergy() {
        return data.get(DATA_USES_ENERGY) != 0;
    }

    public TurretEnableType getEnableType() {
        TurretEnableType[] values = TurretEnableType.values();
        return values[Math.floorMod(data.get(DATA_ENABLE_TYPE), values.length)];
    }

    public PlayerTargeting getPlayerTargeting() {
        PlayerTargeting[] values = PlayerTargeting.values();
        return values[Math.floorMod(data.get(DATA_PLAYER_TARGETING), values.length)];
    }

    public String getOwnerName() {
        return ownerName;
    }

    private static Set<UUID> readAllies(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Set<UUID> read = new HashSet<>();
        for (int index = 0; index < size; index++) read.add(buf.readUUID());
        return read;
    }

    public boolean canModify() {
        return canModify;
    }

    public boolean isAlly(UUID target) {
        return allies.contains(target);
    }

    public void toggleAllyLocally(UUID target) {
        if (!allies.remove(target)) allies.add(target);
    }

    public TurretMode getMode() {
        TurretMode[] values = TurretMode.values();
        return values[Math.floorMod(data.get(DATA_MODE), values.length)];
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int id) {
        if (turret == null || !turret.canInteract(player)) return false;
        if (id == BUTTON_ENABLE_TYPE) {
            turret.setEnableType(turret.getEnableType().next());
            return true;
        }
        if (id == BUTTON_MODE) {
            turret.setMode(turret.getMode().next());
            return true;
        }
        if (id == BUTTON_PLAYER_TARGETING) {
            turret.setPlayerTargeting(turret.getPlayerTargeting().next());
            return true;
        }
        return false;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        if (!canModify) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int turretSlots = container.getContainerSize();

        if (index < turretSlots) {
            if (!moveItemStackTo(stack, turretSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, turretSlots, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return container.stillValid(player);
    }

    private static class TurretMenuData implements ContainerData {
        private final TurretEntity turret;

        private TurretMenuData(TurretEntity turret) {
            this.turret = turret;
        }

        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_HEALTH -> Math.round(turret.getHealth());
                case DATA_MAX_HEALTH -> Math.round(turret.getMaxHealth());
                case DATA_ENERGY_LOW -> turret.getEnergyStored() & 0xFFFF;
                case DATA_ENERGY_HIGH -> (turret.getEnergyStored() >>> 16) & 0xFFFF;
                case DATA_MAX_ENERGY_LOW -> turret.getMaxEnergyStored() & 0xFFFF;
                case DATA_MAX_ENERGY_HIGH -> (turret.getMaxEnergyStored() >>> 16) & 0xFFFF;
                case DATA_ENABLE_TYPE -> turret.getEnableType().ordinal();
                case DATA_MODE -> turret.getMode().ordinal();
                case DATA_USES_ENERGY -> TACZTurretsConfig.requireEnergy ? 1 : 0;
                case DATA_PLAYER_TARGETING -> turret.getPlayerTargeting().ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_SIZE;
        }
    }
}
