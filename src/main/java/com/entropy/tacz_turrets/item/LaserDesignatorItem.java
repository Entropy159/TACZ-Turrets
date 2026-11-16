package com.entropy.tacz_turrets.item;

import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import com.entropy.tacz_turrets.registry.SoundRegistry;
import com.entropy.tacz_turrets.turret.TurretEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//? if neoforge {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
*///?}

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class LaserDesignatorItem extends Item {
    private static final String TURRETS = "Turrets";
    private static final String ACTIVE = "Active";
    private static final Set<UUID> FIRING = new HashSet<>();
    private static final Map<UUID, Aim> AIMS = new HashMap<>();

    public LaserDesignatorItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            if (player.isSecondaryUseActive()) {
                edit(stack, tag -> tag.remove(TURRETS));
                player.displayClientMessage(Component.translatable("message.tacz_turrets.designator.cleared"), true);
            } else {
                boolean active = !isActive(stack);
                edit(stack, tag -> tag.putBoolean(ACTIVE, active));
                player.displayClientMessage(Component.translatable(active ? "message.tacz_turrets.designator.on" : "message.tacz_turrets.designator.off"), true);
                click(player, active ? 1.1F : 0.9F);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static void click(Player player, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundRegistry.LASER_CLICK.get(), SoundSource.PLAYERS, 0.5F, pitch + (player.getRandom().nextFloat() - 0.5F) * 0.1F);
    }

    @Override
    //? if forge {
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("item.tacz_turrets.laser_designator.linked", linked(stack).size()).withStyle(ChatFormatting.GRAY));
        for (String line : List.of("link", "laser", "fire", "clear")) {
            tooltip.add(Component.translatable("item.tacz_turrets.laser_designator.tooltip." + line).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    private static CompoundTag tag(ItemStack stack) {
        //? if forge {
        CompoundTag tag = stack.getTag();
        return tag == null ? new CompoundTag() : tag;
        //?} else {
        /*return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        *///?}
    }

    private static void edit(ItemStack stack, Consumer<CompoundTag> change) {
        //? if forge {
        change.accept(stack.getOrCreateTag());
        //?} else {
        /*CustomData.update(DataComponents.CUSTOM_DATA, stack, change::accept);
        *///?}
    }

    public static boolean isActive(ItemStack stack) {
        return stack.getItem() instanceof LaserDesignatorItem && tag(stack).getBoolean(ACTIVE);
    }

    public static List<UUID> linked(ItemStack stack) {
        List<UUID> ids = new ArrayList<>();
        for (Tag entry : tag(stack).getList(TURRETS, Tag.TAG_INT_ARRAY)) ids.add(NbtUtils.loadUUID(entry));
        return ids;
    }

    public static boolean isLinked(ItemStack stack, UUID turret) {
        return isActive(stack) && linked(stack).contains(turret);
    }

    public static boolean toggleLink(ItemStack stack, UUID turret) {
        List<UUID> ids = linked(stack);
        boolean linked = !ids.remove(turret);
        if (linked) ids.add(turret);
        ListTag list = new ListTag();
        for (UUID id : ids) list.add(NbtUtils.createUUID(id));
        edit(stack, tag -> tag.put(TURRETS, list));
        return linked;
    }

    @Nullable
    public static InteractionHand activeHand(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (isActive(player.getItemInHand(hand))) return hand;
        }
        return null;
    }

    public static ItemStack held(Player player) {
        InteractionHand hand = activeHand(player);
        return hand == null ? ItemStack.EMPTY : player.getItemInHand(hand);
    }

    public static HitResult aim(Player player, float partialTick) {
        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 end = eye.add(player.getViewVector(partialTick).scale(TACZTurretsConfig.maxRange()));
        HitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 stop = block.getLocation();
        EntityHitResult entity = ProjectileUtil.getEntityHitResult(player, eye, stop, new AABB(eye, stop).inflate(1.0D), target -> canHit(player, target), eye.distanceToSqr(stop));
        if (entity == null) return block;
        return TACZTurretsConfig.LASER_LOCK_ON.get() ? new EntityHitResult(entity.getEntity(), entity.getEntity().getBoundingBox().getCenter()) : entity;
    }

    private static boolean canHit(Player player, Entity target) {
        if (target.isSpectator() || !target.isPickable()) return false;
        return !(target instanceof TurretEntity turret) || turret.isSentry() || !turret.isOwnerId(player.getUUID());
    }

    public static HitResult serverAim(Player player) {
        long time = player.level().getGameTime();
        Aim cached = AIMS.get(player.getUUID());
        if (cached != null && cached.time == time && cached.level == player.level()) return cached.hit;
        HitResult hit = aim(player, 1.0F);
        AIMS.put(player.getUUID(), new Aim(player.level(), time, hit));
        return hit;
    }

    public static void setFiring(Player player, boolean firing) {
        if (firing) {
            if (FIRING.add(player.getUUID()) && isActive(player.getMainHandItem())) click(player, 1.0F);
        } else {
            FIRING.remove(player.getUUID());
        }
    }

    public static boolean isFiring(Player player) {
        return FIRING.contains(player.getUUID()) && isActive(player.getMainHandItem());
    }

    public static void forget(Player player) {
        FIRING.remove(player.getUUID());
        AIMS.remove(player.getUUID());
    }

    private record Aim(Level level, long time, HitResult hit) {
    }
}
