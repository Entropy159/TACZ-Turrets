package com.entropy.tacz_turrets.turret;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.compat.FTBTeamsCompat;
import com.entropy.tacz_turrets.compat.OpenPACCompat;
import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import com.entropy.tacz_turrets.menu.TurretLayout;
import com.entropy.tacz_turrets.menu.TurretMenu;
import com.entropy.tacz_turrets.registry.EntityTypeRegistry;
import com.entropy.tacz_turrets.registry.ItemRegistry;
import com.entropy.tacz_turrets.registry.SoundRegistry;
import com.entropy.tacz_turrets.registry.TagRegistry;
import com.entropy.tacz_turrets.turret.ai.TaczShootAttack;
import com.entropy.tacz_turrets.util.Enums;
import com.entropy.tacz_turrets.util.RegistryFilter;
import com.entropy.tacz_turrets.util.TurretAllies;
import com.entropy.tacz_turrets.util.TurretEnergyStorage;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
//? if forge {
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
//?} else {
/*import net.minecraft.world.level.Explosion;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
*///?}
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.*;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.object.FreePositionTracker;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
//? if forge {
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
//?} else {
/*import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
*///?}
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class TurretEntity extends Mob implements SmartBrainOwner<TurretEntity>, GeoEntity, MenuProvider {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final IGunOperator gunOperator = IGunOperator.fromLivingEntity(this);

    private static final boolean FTB_TEAMS = ModList.get().isLoaded("ftbteams");
    private static final boolean OPEN_PAC = ModList.get().isLoaded("openpartiesandclaims");
    private static final int TARGET_SPREAD_INTERVAL = 10;
    private static final int TARGET_LOST_LOOK_TICKS = 60;
    private static final double TARGET_SPREAD_REACH = 4.0D;
    private static final double TARGET_SPREAD_MIN_RADIUS = 16.0D;
    private static final double TARGET_SWITCH_MARGIN = 0.5D;
    private static final int CONSERVATIVE_SHOT_INTERVAL = 8;
    private static final int RECOIL_TICKS = 3;
    private static final float RECOIL_DEGREES = 9.0F;
    private static final float RECOIL_PUSH = 0.09F;
    private static final int REPAIR_INTERACT_GRACE = 10;
    private static final int RETALIATE_UNTIL_DEATH = -1;
    private static final EntityDataAccessor<Integer> RECOIL = SynchedEntityData.defineId(TurretEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(TurretEntity.class, EntityDataSerializers.INT);
    private boolean gunDrawn = false;
    private TurretEnableType enableType = TurretEnableType.ALWAYS_ON;
    private TurretMode mode = TurretMode.AGGRESSIVE;
    private PlayerTargeting playerTargeting = PlayerTargeting.RETALIATE;
    private String ownerName = "";
    private int lastShotTick = 0;
    private boolean hadTarget = false;
    private int lastRepairTick = -100;
    private UUID retaliateTarget;
    private int retaliateTicks = 0;
    private final ItemStackHandler inventory = new ItemStackHandler(Math.max(1, TACZTurretsConfig.TURRET_SLOT_ROWS.get() * TACZTurretsConfig.TURRET_SLOT_LENGTH.get()));
    private final List<ItemStack> overflow = new ArrayList<>();
    //? if forge
    private final LazyOptional<ItemStackHandler> lazyInventory = LazyOptional.of(() -> inventory);
    private final TurretEnergyStorage energy = new TurretEnergyStorage(TACZTurretsConfig.ENERGY_CAPACITY.get(), TACZTurretsConfig.ENERGY_TRANSFER_RATE.get());
    //? if forge
    private final LazyOptional<TurretEnergyStorage> lazyEnergy = LazyOptional.of(() -> energy);
    public UUID owner;

    public TurretEntity(Level level, BlockPos pos, Player player) {
        this(EntityTypeRegistry.TURRET.get(), level);
        setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        owner = player.getUUID();
        ownerName = player.getGameProfile().getName();
    }

    public TurretEntity(EntityType<? extends TurretEntity> type, Level level) {
        super(type, level);
        gunOperator.initialData();
    }

    @Override
    //? if forge {
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(STATE, TurretState.NO_GUN.ordinal());
        entityData.define(RECOIL, 0);
    }
    //?} else {
    /*protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, TurretState.NO_GUN.ordinal());
        builder.define(RECOIL, 0);
    }
    *///?}

    //? if forge {
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyInventory.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return lazyEnergy.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyInventory.invalidate();
        lazyEnergy.invalidate();
    }
    //?}

    public static AttributeSupplier.@NotNull Builder createLivingAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, Math.max(TACZTurretsConfig.TURRET_RANGE.getDefault(), TACZTurretsConfig.SNIPER_TURRET_RANGE.getDefault())).add(Attributes.ARMOR, TACZTurretsConfig.TURRET_ARMOR.getDefault()).add(Attributes.MAX_HEALTH, TACZTurretsConfig.TURRET_HEALTH.getDefault());
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        //? if forge {
        tag.put("Inventory", inventory.serializeNBT());
        //?} else {
        /*tag.put("Inventory", inventory.serializeNBT(level().registryAccess()));
        *///?}
        if (owner != null) tag.putUUID("Owner", owner);
        tag.putString("EnableType", enableType.name());
        tag.putString("Mode", mode.name());
        tag.putString("PlayerTargeting", playerTargeting.name());
        tag.putString("OwnerName", ownerName);
        tag.putInt("Energy", energy.getEnergyStored());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        //? if forge {
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        //?} else {
        /*inventory.deserializeNBT(level().registryAccess(), tag.getCompound("Inventory"));
        *///?}
        resizeInventory();
        if (tag.contains("Owner")) owner = tag.getUUID("Owner");
        enableType = Enums.byName(tag.getString("EnableType"), TurretEnableType.ALWAYS_ON);
        mode = Enums.byName(tag.getString("Mode"), TurretMode.AGGRESSIVE);
        playerTargeting = Enums.byName(tag.getString("PlayerTargeting"), PlayerTargeting.RETALIATE);
        ownerName = tag.getString("OwnerName");
        energy.setEnergy(tag.getInt("Energy"));
    }

    public ItemStack getGunStack() {
        return getMainHandItem();
    }

    public void setGunStack(ItemStack stack) {
        setItemSlot(EquipmentSlot.MAINHAND, stack);
    }

    public ModernKineticGunItem getGunItem() {
        return hasGun() ? (ModernKineticGunItem) getGunStack().getItem() : null;
    }

    public boolean hasMinigun() {
        return hasGun() && TimelessAPI.getGunDisplay(getGunStack()).map(display -> display.getThirdPersonAnimation().equals("minigun")).orElse(false);
    }

    public boolean hasGun() {
        return getGunStack().getItem() instanceof ModernKineticGunItem;
    }

    public boolean gunHasAmmo() {
        if (!hasGun()) return false;
        if (getGunItem().useInventoryAmmo(getGunStack())) {
            return getGunItem().hasInventoryAmmo(this, getGunStack(), gunOperator.needCheckAmmo());
        }
        return getGunItem().getCurrentAmmoCount(getGunStack()) > 0 || hasChamberedRound();
    }

    private boolean hasChamberedRound() {
        ItemStack gunStack = getGunStack();
        IGun iGun = IGun.getIGunOrNull(gunStack);
        if (iGun == null || !iGun.hasBulletInBarrel(gunStack)) return false;
        return TimelessAPI.getCommonGunIndex(iGun.getGunId(gunStack))
                .map(index -> index.getGunData().getBolt() != Bolt.OPEN_BOLT)
                .orElse(false);
    }

    public boolean isEnabled() {
        return TurretState.getState(this) != TurretState.DISABLED;
    }

    public boolean isSniper() {
        if (!hasGun()) return false;
        return TimelessAPI.getCommonGunIndex(getGunItem().getGunId(getGunStack()))
                .map(index -> TACZTurretsConfig.sniperGunTypes.contains(index.getType()))
                .orElse(false);
    }

    public double getRange() {
        return isSniper() ? TACZTurretsConfig.SNIPER_TURRET_RANGE.get() : TACZTurretsConfig.TURRET_RANGE.get();
    }

    public void tryShoot() {
        if (!isEnabled()) return;
        if (isConservingAmmo() && tickCount - lastShotTick < CONSERVATIVE_SHOT_INTERVAL) return;
        gunOperator.aim(true);
        ShootResult result = shoot();
        switch (result) {
            case SUCCESS -> {
                lastShotTick = tickCount;
                if (TACZTurretsConfig.TURRET_RECOIL.get()) entityData.set(RECOIL, RECOIL_TICKS);
                if (TACZTurretsConfig.REQUIRE_ENERGY.get()) energy.consume(TACZTurretsConfig.ENERGY_PER_SHOT.get());
            }
            case NEED_BOLT -> gunOperator.bolt();
            case NO_AMMO -> {
                collectAmmo();
                gunOperator.reload();
            }
            case NOT_DRAW -> gunOperator.draw(this::getGunStack);
        }
        if (TACZTurretsConfig.LOG_TURRET_SHOOT_RESULTS.get()) TACZTurrets.LOGGER.info("Turret shoot result {}", result);
    }

    private ShootResult shoot() {
        float inaccuracy = getInaccuracy();
        if (inaccuracy <= 0.0F) {
            return gunOperator.shoot(() -> getViewXRot(1), () -> getViewYRot(1));
        }
        float pitchOffset = (float) random.nextGaussian() * inaccuracy;
        float yawOffset = (float) random.nextGaussian() * inaccuracy;
        return gunOperator.shoot(() -> getViewXRot(1) + pitchOffset, () -> getViewYRot(1) + yawOffset);
    }

    private float getInaccuracy() {
        return switch (TACZTurretsConfig.INACCURACY_MODE.get()) {
            case RANDOM -> TACZTurretsConfig.RANDOM_INACCURACY.get().floatValue();
            case DISTANCE -> {
                LivingEntity target = BrainUtils.getTargetOfEntity(this);
                if (target == null) yield 0.0F;
                double distance = Math.sqrt(distanceToSqr(target));
                yield (float) (TACZTurretsConfig.DISTANCE_INACCURACY.get() * Math.min(1.0D, distance / getRange()));
            }
        };
    }

    public boolean hasAmmo() {
        if (!gunOperator.consumesAmmoOrNot()) return true;
        if (gunHasAmmo()) return true;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (isRightAmmo(inventory.getStackInSlot(slot))) {
                return true;
            }
        }
        return false;
    }

    public boolean isRightAmmo(ItemStack stack) {
        if (stack.getItem() instanceof IAmmoBox ammoBox) {
            if (ammoBox.isAllTypeCreative(stack)) {
                return true;
            }
            return hasGun() && ammoBox.isAmmoBoxOfGun(getGunStack(), stack);
        }
        return hasGun() && stack.getItem() instanceof IAmmo ammo && ammo.isAmmoOfGun(getGunStack(), stack);
    }

    public boolean hasCreativeAmmo() {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (isCreativeAmmo(inventory.getStackInSlot(slot))) {
                return true;
            }
        }
        return false;
    }

    public boolean isCreativeAmmo(ItemStack stack) {
        return stack.getItem() instanceof IAmmoBox ammoBox && (ammoBox.isCreative(stack) || ammoBox.isAllTypeCreative(stack));
    }

    @Nullable
    private BlockEntity getSupplyBlockEntity() {
        BlockEntity blockEntity = level().getBlockEntity(blockPosition());
        return blockEntity == null ? level().getBlockEntity(blockPosition().below()) : blockEntity;
    }

    //? if forge {
    @Nullable
    private <T> T getSupplyCapability(Capability<T> capability) {
        BlockEntity blockEntity = getSupplyBlockEntity();
        if (blockEntity == null) return null;
        LazyOptional<T> sided = blockEntity.getCapability(capability, Direction.UP);
        return (sided.isPresent() ? sided : blockEntity.getCapability(capability)).orElse(null);
    }
    //?} else {
    /*@Nullable
    private <T> T getSupplyCapability(BlockCapability<T, Direction> capability) {
        BlockEntity blockEntity = getSupplyBlockEntity();
        if (blockEntity == null) return null;
        T sided = level().getCapability(capability, blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity, Direction.UP);
        return sided != null ? sided : level().getCapability(capability, blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity, null);
    }
    *///?}

    public void collectAmmo() {
        if (!shouldCollectAmmo()) return;
        //? if forge {
        IItemHandler handler = getSupplyCapability(ForgeCapabilities.ITEM_HANDLER);
        //?} else {
        /*IItemHandler handler = getSupplyCapability(Capabilities.ItemHandler.BLOCK);
        *///?}
        if (handler == null) return;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!isRightAmmo(stack)) continue;
            int fits = stack.getCount() - ItemHandlerHelper.insertItemStacked(inventory, stack, true).getCount();
            if (fits <= 0) continue;
            ItemStack extracted = handler.extractItem(slot, fits, false);
            ItemHandlerHelper.insertItemStacked(inventory, extracted, false);
            if (isCreativeAmmo(extracted)) return;
        }
    }

    public boolean shouldCollectAmmo() {
        return gunOperator.consumesAmmoOrNot() && isEnabled() && !hasCreativeAmmo();
    }

    public boolean hasEnoughEnergy() {
        if (!TACZTurretsConfig.REQUIRE_ENERGY.get()) return true;
        return energy.getEnergyStored() >= Math.max(1, TACZTurretsConfig.ENERGY_PER_SHOT.get());
    }

    private void tickEnergy() {
        if (!TACZTurretsConfig.REQUIRE_ENERGY.get()) return;
        if (TACZTurretsConfig.ENERGY_IDLE_DRAIN.get() > 0 && !enableType.shouldDisable(level(), blockPosition())) {
            energy.consume(TACZTurretsConfig.ENERGY_IDLE_DRAIN.get());
        }
        collectEnergy();
    }

    private void collectEnergy() {
        int space = energy.getMaxEnergyStored() - energy.getEnergyStored();
        if (space <= 0) return;
        //? if forge {
        IEnergyStorage source = getSupplyCapability(ForgeCapabilities.ENERGY);
        //?} else {
        /*IEnergyStorage source = getSupplyCapability(Capabilities.EnergyStorage.BLOCK);
        *///?}
        if (source == null) return;
        int available = source.extractEnergy(space, true);
        int accepted = energy.receiveEnergy(available, true);
        if (accepted > 0) energy.receiveEnergy(source.extractEnergy(accepted, false), false);
    }

    private void syncAttributes() {
        if (getAttributeBaseValue(Attributes.ARMOR) != TACZTurretsConfig.TURRET_ARMOR.get()) getAttribute(Attributes.ARMOR).setBaseValue(TACZTurretsConfig.TURRET_ARMOR.get());
        if (getAttributeBaseValue(Attributes.MAX_HEALTH) == TACZTurretsConfig.TURRET_HEALTH.get()) return;
        float fraction = getHealth() / getMaxHealth();
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(TACZTurretsConfig.TURRET_HEALTH.get());
        setHealth(fraction * getMaxHealth());
    }

    private void tickPassiveHealing() {
        if (!TACZTurretsConfig.PASSIVE_HEALING.get()) return;
        if (getHealth() >= getMaxHealth()) return;
        if (tickCount % TACZTurretsConfig.PASSIVE_HEAL_INTERVAL.get() != 0) return;
        heal(TACZTurretsConfig.PASSIVE_HEAL_AMOUNT.get().floatValue());
    }

    @Override
    public void tick() {
        super.tick();
        if (getTarget() != null && !getTarget().isAlive()) {
            setTarget(null);
        }
        onTickServerSide();

        if (!level().isClientSide() && hasGun() && !gunHasAmmo() && !gunOperator.getSynReloadState().getStateType().isReloading()) {
            gunOperator.reload();
        }
    }

    public boolean isConservingAmmo() {
        if (mode == TurretMode.CONSERVATIVE) return true;
        if (mode != TurretMode.ADAPTIVE) return false;
        LivingEntity target = BrainUtils.getTargetOfEntity(this);
        if (target == null) return false;
        return distanceToSqr(target) > (double) TACZTurretsConfig.ADAPTIVE_RANGE.get() * TACZTurretsConfig.ADAPTIVE_RANGE.get();
    }

    private float getRecoilProgress(float partialTick) {
        int recoil = entityData.get(RECOIL);
        if (recoil <= 0) return 0.0F;
        return Mth.clamp((recoil - partialTick) / RECOIL_TICKS, 0.0F, 1.0F);
    }

    public float getRecoilDegrees(float partialTick) {
        if (TACZTurretsConfig.RECOIL_TYPE.get() != RecoilType.BOUNCE) return 0.0F;
        return RECOIL_DEGREES * getRecoilProgress(partialTick);
    }

    public float getRecoilPush(float partialTick) {
        if (TACZTurretsConfig.RECOIL_TYPE.get() != RecoilType.PUSH) return 0.0F;
        return RECOIL_PUSH * getRecoilProgress(partialTick);
    }

    private void onTickServerSide() {
        if (!level().isClientSide()) {
            dropOverflow();
            int recoil = entityData.get(RECOIL);
            if (recoil > 0) entityData.set(RECOIL, recoil - 1);
            tickEnergy();
            syncAttributes();
            tickPassiveHealing();
            if (isEnabled()) {
                if (hasGun()) {
                    if (!gunDrawn) {
                        gunOperator.draw(this::getGunStack);
                        gunDrawn = true;
                    }
                    if (gunOperator.getSynReloadState().getStateType().isReloading()) {
                        TurretState.RELOADING.setState(this);
                    } else if (hasAmmo()) {
                        TurretState.ACTIVE.setState(this);
                    } else {
                        TurretState.NO_AMMO.setState(this);
                        collectAmmo();
                    }
                } else {
                    TurretState.NO_GUN.setState(this);
                }
                if (shouldDisable()) {
                    TurretState.DISABLED.setState(this);
                }
            } else if (!shouldDisable()) {
                TurretState.NO_GUN.setState(this);
            }
        }
    }

    private boolean shouldDisable() {
        return enableType.shouldDisable(level(), blockPosition()) || !hasEnoughEnergy();
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        boolean manages = canInteract(player);
        if (!manages && player.isCrouching()) {
            return super.mobInteract(player, hand);
        }
        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (manages) {
            InteractionResult result = manageInteract(player, hand);
            if (result != null) return result;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            TurretLayout layout = TurretLayout.fromConfig();
            //? if forge {
            NetworkHooks.openScreen(serverPlayer, this, buf -> writeMenuData(buf, layout, manages));
            //?} else {
            /*serverPlayer.openMenu(this, buf -> writeMenuData(buf, layout, manages));
            *///?}
        }
        return InteractionResult.SUCCESS;
    }

    private void writeMenuData(FriendlyByteBuf buf, TurretLayout layout, boolean manages) {
        buf.writeVarInt(getId());
        buf.writeByte(layout.rows);
        buf.writeByte(layout.columns);
        buf.writeUtf(getOwnerName());
        buf.writeCollection(getAllies(), (out, ally) -> out.writeUUID(ally));
        buf.writeBoolean(manages);
    }

    @Nullable
    private InteractionResult manageInteract(Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (!player.isCrouching() && TACZTurretsConfig.repairItems.matches(heldStack.getItem())) {
            if (getHealth() < getMaxHealth()) {
                heal(TACZTurretsConfig.REPAIR_AMOUNT.get().floatValue());
                if (!player.isCreative()) heldStack.shrink(1);
                playRepairSound();
                spawnRepairParticles();
            }
            lastRepairTick = tickCount;
            return InteractionResult.SUCCESS;
        }
        if (player.isCrouching()) {
            ItemStack gunStack = getGunStack();
            setGunStack(ItemStack.EMPTY);
            if (!gunStack.isEmpty() && !player.getInventory().add(gunStack)) {
                spawnAtLocation(gunStack);
            }
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack slotStack = inventory.extractItem(slot, inventory.getStackInSlot(slot).getCount(), false);
                if (!slotStack.isEmpty() && !player.getInventory().add(slotStack)) {
                    spawnAtLocation(slotStack);
                }
            }
            if (!player.isCreative()) {
                player.getInventory().add(new ItemStack(ItemRegistry.TURRET.get()));
            }
            playTurretSound(SoundRegistry.TURRET_PICKUP.get());
            discard();
            return InteractionResult.SUCCESS;
        }
        if (tickCount - lastRepairTick < REPAIR_INTERACT_GRACE) {
            return InteractionResult.SUCCESS;
        }
        return null;
    }

    public void playTurretSound(SoundEvent sound) {
        if (!TACZTurretsConfig.ENABLE_SOUNDS.get()) return;
        playSound(sound, 1.0F, 0.9F + random.nextFloat() * 0.2F);
    }

    private void playRepairSound() {
        if (!TACZTurretsConfig.ENABLE_SOUNDS.get()) return;
        List<? extends String> sounds = TACZTurretsConfig.REPAIR_SOUNDS.get();
        if (sounds.isEmpty()) return;
        ResourceLocation soundId = ResourceLocation.tryParse(sounds.get(random.nextInt(sounds.size())));
        if (soundId == null) return;
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(soundId);
        if (sound != null) playSound(sound, 1.0F, 0.9F + random.nextFloat() * 0.2F);
    }

    private void spawnRepairParticles() {
        if (!TACZTurretsConfig.REPAIR_PARTICLES.get()) return;
        if (!(level() instanceof ServerLevel serverLevel)) return;
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + getBbHeight() * 0.6D, getZ(), 12, getBbWidth() * 0.5D, getBbHeight() * 0.4D, getBbWidth() * 0.5D, 0.0D);
    }

    private void resizeInventory() {
        int desired = Math.max(1, TACZTurretsConfig.TURRET_SLOT_ROWS.get() * TACZTurretsConfig.TURRET_SLOT_LENGTH.get());
        if (inventory.getSlots() == desired) return;
        List<ItemStack> kept = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSlots(); slot++) kept.add(inventory.getStackInSlot(slot));
        inventory.setSize(desired);
        for (int slot = 0; slot < kept.size(); slot++) {
            ItemStack stack = kept.get(slot);
            if (stack.isEmpty()) continue;
            if (slot < desired) {
                inventory.setStackInSlot(slot, stack);
            } else {
                overflow.add(stack);
            }
        }
    }

    private void dropOverflow() {
        if (overflow.isEmpty()) return;
        for (ItemStack stack : overflow) spawnAtLocation(stack);
        overflow.clear();
    }

    @Override
    public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new TurretMenu(containerId, playerInventory, this, TurretLayout.fromConfig(), getOwnerName(), getAllies(), canInteract(player));
    }

    public boolean canInteract(Player player) {
        return isOwnedBy(player) || (TACZTurretsConfig.ALLIES_HAVE_PERMS.get() && isAlliedWithOwner(player));
    }

    public boolean isOwnedBy(Player player) {
        return owner == null || player.getUUID().equals(owner) || player.isCreative() || (TACZTurretsConfig.OP_BYPASS.get() && player.hasPermissions(2));
    }

    public boolean canAcceptAmmo(ItemStack stack) {
        if (isRightAmmo(stack)) return true;
        return !hasGun() && (stack.getItem() instanceof IAmmo || stack.getItem() instanceof IAmmoBox);
    }

    public boolean isGun(ItemStack stack) {
        return stack.getItem() instanceof ModernKineticGunItem;
    }

    public void onInventoryChanged() {
        gunDrawn = false;
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcastAndSend(this, new ClientboundSetEquipmentPacket(getId(), List.of(Pair.of(EquipmentSlot.MAINHAND, getMainHandItem()))));
        }
    }

    public TurretEnableType getEnableType() {
        return enableType;
    }

    public void setEnableType(TurretEnableType type) {
        enableType = type;
    }

    public TurretMode getMode() {
        return mode;
    }

    public void setMode(TurretMode turretMode) {
        mode = turretMode;
    }

    public int getEnergyStored() {
        return energy.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energy.getMaxEnergyStored();
    }

    public TurretEnergyStorage getEnergyStorage() {
        return energy;
    }

    @Override
    //? if forge {
    protected void dropCustomDeathLoot(@NotNull DamageSource source, int looting, boolean recentlyHit) {
    //?} else {
    /*protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource source, boolean recentlyHit) {
    *///?}
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) spawnAtLocation(inventory.extractItem(i, inventory.getStackInSlot(i).getCount(), false));
        }
        if (hasGun()) spawnAtLocation(getGunStack());
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (source.getEntity() instanceof TurretEntity) {
            return false;
        }
        if (source.getEntity() instanceof LivingEntity entity) {
            if (entity instanceof Player player) markRetaliation(player);
            if (isValidTarget(entity)) {
                alertTo(entity);
                for (TurretEntity turret : level().getEntitiesOfClass(TurretEntity.class, AABB.ofSize(position(), 64, 16, 64), e -> e.getSensing().hasLineOfSight(entity) || BehaviorUtils.entityIsVisible(e.getBrain(), entity))) {
                    if (entity instanceof Player player && Objects.equals(turret.owner, owner)) turret.markRetaliation(player);
                    if (turret.isValidTarget(entity)) turret.alertTo(entity);
                }
            }
        }

        return super.hurt(source, damage);
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        if (source.getEntity() != null && source.getEntity().getUUID().equals(owner)) {
            return false;
        }
        if (!TACZTurretsConfig.TURRETS_TAKE_DAMAGE.get()) {
            return !source.isCreativePlayer() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        }
        return super.isInvulnerableTo(source);
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource source) {
        return TACZTurretsConfig.ENABLE_SOUNDS.get() ? SoundRegistry.TURRET_HURT.get() : null;
    }

    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance effect) {
        //? if forge {
        return !TACZTurretsConfig.effectBlacklist.matches(effect.getEffect()) && super.canBeAffected(effect);
        //?} else {
        /*return !TACZTurretsConfig.effectBlacklist.matches(effect.getEffect().value()) && super.canBeAffected(effect);
        *///?}
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ItemRegistry.TURRET.get());
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    public void setTarget(@Nullable LivingEntity entity) {
        if (!isEnabled()) return;
        if (entity instanceof Player player) setLastHurtByPlayer(player);
        super.setTarget(entity);
    }

    protected Brain.@NotNull Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep() {
        if (!hasGun() || !isEnabled()) return;
        if (retaliateTicks < 0 && TACZTurretsConfig.RETALIATE_TARGETING.get() != RetaliateTargeting.CLEAR_ON_DEATH) retaliateTicks = retaliationTicks();
        if (retaliateTicks > 0 && --retaliateTicks == 0) retaliateTarget = null;
        tickBrain(this);
        retargetImmediately();
        spreadTargets();
    }

    private void retargetImmediately() {
        LivingEntity current = BrainUtils.getTargetOfEntity(this);
        if (current != null && current.isAlive()) {
            hadTarget = true;
            return;
        }
        if (!hadTarget) return;
        hadTarget = false;
        PositionTracker lastLook = BrainUtils.getMemory(this, MemoryModuleType.LOOK_TARGET);
        if (lastLook != null) BrainUtils.setForgettableMemory(this, MemoryModuleType.LOOK_TARGET, new FreePositionTracker(lastLook.currentPosition()), TARGET_LOST_LOOK_TICKS + random.nextInt(40));
        if (!isEnabled()) return;
        NearestVisibleLivingEntities visible = BrainUtils.getMemory(this, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        if (visible != null) visible.findClosest(this::shouldTarget).ifPresent(this::alertTo);
    }

    private void spreadTargets() {
        if (!TACZTurretsConfig.BETTER_TARGETING.get()) return;
        if (tickCount % TARGET_SPREAD_INTERVAL != 0) return;

        LivingEntity current = BrainUtils.getTargetOfEntity(this);
        NearestVisibleLivingEntities visible = BrainUtils.getMemory(this, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        if (current == null || visible == null) return;

        double range = getRange();
        double currentDistance = distanceToSqr(current);
        double reachSquared = Math.min(range * range, Math.max(currentDistance * TARGET_SPREAD_REACH, TARGET_SPREAD_MIN_RADIUS * TARGET_SPREAD_MIN_RADIUS));

        Map<LivingEntity, Integer> claims = new HashMap<>();
        for (TurretEntity ally : level().getEntitiesOfClass(TurretEntity.class, getBoundingBox().inflate(range), turret -> turret != this)) {
            LivingEntity target = BrainUtils.getTargetOfEntity(ally);
            if (target != null) claims.merge(target, 1, Integer::sum);
        }
        int currentClaims = claims.getOrDefault(current, 0);

        LivingEntity best = current;
        int bestClaims = currentClaims;
        double bestDistance = currentDistance;
        for (LivingEntity candidate : visible.findAll(entity -> entity != current && distanceToSqr(entity) <= reachSquared && shouldTarget(entity))) {
            int candidateClaims = claims.getOrDefault(candidate, 0);
            double distance = distanceToSqr(candidate);
            if (candidateClaims < bestClaims || (candidateClaims == bestClaims && distance < bestDistance)) {
                best = candidate;
                bestClaims = candidateClaims;
                bestDistance = distance;
            }
        }

        if (best != current && (bestClaims < currentClaims || bestDistance < currentDistance * TARGET_SWITCH_MARGIN)) {
            alertTo(best);
        }
    }

    @Override
    public BrainActivityGroup<? extends TurretEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(new TargetOrRetaliate<TurretEntity>().isAllyIf((e, l) -> l instanceof TurretEntity).attackablePredicate(l -> l != null && isValidTarget(l)).alertAlliesWhen((m, e) -> e instanceof LivingEntity living && m.isValidTarget(living) && m.getSensing().hasLineOfSight(living)).runFor(e -> 999), new LookAtTarget<TurretEntity>().runFor(entity -> entity.getRandom().nextInt(40, 300)));
    }

    public BrainActivityGroup<? extends TurretEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(new FirstApplicableBehaviour<TurretEntity>(new TargetOrRetaliate<TurretEntity>().attackablePredicate(l -> l != null && isValidTarget(l)), new SetPlayerLookTarget<>(), idleLook()), new Idle<TurretEntity>().runFor(entity -> entity.getRandom().nextInt(30, 60)));
    }

    private static ExtendedBehaviour<TurretEntity> idleLook() {
        return new SetRandomLookTarget<TurretEntity>().lookChance(ConstantFloat.of(1.0F)).lookTime(entity -> 40 + entity.getRandom().nextInt(60)).startCondition(entity -> TACZTurretsConfig.IDLE_LOOK_AROUND.get());
    }

    public BrainActivityGroup<? extends TurretEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(new InvalidateAttackTarget<TurretEntity>().invalidateIf((entity, target) -> !target.isAlive() || (target instanceof Player player && player.getAbilities().invulnerable) || !entity.getSensing().hasLineOfSight(target) || !entity.isValidTarget(target)).ignoreFailedPathfinding(), new SetRetaliateTarget<>(), new TaczShootAttack<>().startCondition(entity -> getMainHandItem().is(ModItems.MODERN_KINETIC_GUN.get()) && gunOperator.getSynShootCoolDown() == 0));
    }

    @Override
    public List<? extends ExtendedSensor<? extends TurretEntity>> getSensors() {
        int range = Math.max(TACZTurretsConfig.TURRET_RANGE.get(), TACZTurretsConfig.SNIPER_TURRET_RANGE.get());
        return ObjectArrayList.of(new NearbyPlayersSensor<TurretEntity>().setRadius(range).setPredicate((p, e) -> e.lastHurtByPlayer != null && p.getUUID().equals(e.lastHurtByPlayer.getUUID()) && e.isValidTarget(p)), new HurtBySensor<>(), new NearbyLivingEntitySensor<TurretEntity>().setRadius(range).setPredicate((target, entity) -> shouldTarget(target)));
    }

    @Nullable
    public Player getOwnerPlayer() {
        return owner == null ? null : level().getPlayerByUUID(owner);
    }

    public void alertTo(LivingEntity target) {
        if (!isEnabled()) return;
        setTarget(target);
        getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
    }

    public boolean isAlliedWithOwner(LivingEntity target) {
        MinecraftServer server = level().getServer();
        if (owner != null && server != null && TurretAllies.get(server).isAlly(owner, target.getUUID())) return true;
        if (!TACZTurretsConfig.RESPECT_TEAMS.get()) return false;
        if (owner != null && server != null && target instanceof Player && isPartyAlly(server, target.getUUID())) return true;
        if (target.getTeam() == null) return false;
        if (isAlliedTo(target)) return true;
        Player ownerPlayer = getOwnerPlayer();
        return ownerPlayer != null && ownerPlayer.isAlliedTo(target);
    }

    private boolean isPartyAlly(MinecraftServer server, UUID target) {
        return (FTB_TEAMS && FTBTeamsCompat.isAlly(owner, target)) || (OPEN_PAC && OpenPACCompat.isAlly(server, owner, target));
    }

    private boolean canTargetPlayers() {
        return TACZTurretsConfig.DAMAGE_PLAYERS.get() && playerTargeting != PlayerTargeting.NEVER;
    }

    public void markRetaliation(Player player) {
        retaliateTarget = player.getUUID();
        retaliateTicks = TACZTurretsConfig.RETALIATE_TARGETING.get() == RetaliateTargeting.CLEAR_ON_DEATH ? RETALIATE_UNTIL_DEATH : retaliationTicks();
    }

    private static int retaliationTicks() {
        return Math.max(1, TACZTurretsConfig.RETALIATION_TIMER.get() * 20);
    }

    public void forgetRetaliation(UUID target) {
        if (target.equals(retaliateTarget)) {
            retaliateTarget = null;
            retaliateTicks = 0;
        }
    }

    private boolean isRetaliating(LivingEntity target) {
        return retaliateTicks != 0 && target.getUUID().equals(retaliateTarget);
    }

    private boolean canEngagePlayer(Player player) {
        return playerTargeting == PlayerTargeting.ALL || isRetaliating(player);
    }

    public PlayerTargeting getPlayerTargeting() {
        return playerTargeting;
    }

    public void setPlayerTargeting(PlayerTargeting targeting) {
        playerTargeting = targeting;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Set<UUID> getAllies() {
        if (owner == null || level().getServer() == null) return Set.of();
        return TurretAllies.get(level().getServer()).getAllies(owner);
    }

    public boolean isValidTarget(LivingEntity target) {
        if (!isEnabled()) return false;
        if (target == this || !target.isAlive()) return false;
        if (target instanceof TurretEntity) return false;
        double range = getRange();
        if (distanceToSqr(target) > range * range) return false;
        if (target.getUUID().equals(owner)) return false;
        if (isAlliedWithOwner(target)) return false;
        if (target.getType().is(TagRegistry.TURRET_IGNORED)) return false;
        if (TACZTurretsConfig.targetBlacklist.matches(target.getType())) return false;

        if (target instanceof Player player) return canTargetPlayers() && !player.isCreative() && !player.isSpectator() && canEngagePlayer(player);
        return true;
    }

    public boolean isProtectedFromFire(LivingEntity victim) {
        if (!TACZTurretsConfig.DAMAGE_PLAYERS.get() && victim instanceof Player) return true;
        if (TACZTurretsConfig.OWNER_TAKES_NO_DAMAGE.get() && victim.getUUID().equals(owner)) return true;
        if (TACZTurretsConfig.ALLIES_CANNOT_BE_DAMAGED.get() && isAlliedWithOwner(victim)) return true;
        return !canDamage(victim);
    }

    public boolean canDamage(LivingEntity victim) {
        if (victim instanceof Player || victim == getTarget() || victim == getLastHurtByMob()) return true;
        RegistryFilter<EntityType<?>> filter = TACZTurretsConfig.damageableEntities;
        return filter.isEmpty() ? isTargetableType(victim) : filter.matches(victim.getType());
    }

    private boolean shouldTarget(LivingEntity target) {
        if (!isValidTarget(target)) return false;
        if (target instanceof Player) return true;
        if (target == getTarget()) return true;
        return isTargetableType(target);
    }

    private boolean isTargetableType(LivingEntity target) {
        if (target.getType().is(TagRegistry.TURRET_IGNORED)) return false;
        if (TACZTurretsConfig.targetBlacklist.matches(target.getType())) return false;
        if (TACZTurretsConfig.targetWhitelist.matches(target.getType())) return true;
        if (target.getType().is(TagRegistry.TURRET_TARGETS)) return true;
        if (TACZTurretsConfig.TARGET_ALL_MOBS.get()) return true;
        if (target instanceof Monster) return true;
        return target.getType().getCategory() == MobCategory.MONSTER;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void knockback(double pStrength, double pX, double pZ) {

    }

    @Override
    //? if forge {
    public boolean ignoreExplosion() {
    //?} else {
    /*public boolean ignoreExplosion(@NotNull Explosion explosion) {
    *///?}
        return true;
    }
}
