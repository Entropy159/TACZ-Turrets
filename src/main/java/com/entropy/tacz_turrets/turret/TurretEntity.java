package com.entropy.tacz_turrets.turret;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import com.entropy.tacz_turrets.menu.TurretLayout;
import com.entropy.tacz_turrets.menu.TurretMenu;
import com.entropy.tacz_turrets.registry.ItemRegistry;
import com.entropy.tacz_turrets.registry.SoundRegistry;
import com.entropy.tacz_turrets.registry.TagRegistry;
import com.entropy.tacz_turrets.turret.ai.TaczShootAttack;
import com.entropy.tacz_turrets.util.HasTurretInventory;
import com.entropy.tacz_turrets.util.TargetFilter;
import com.entropy.tacz_turrets.util.TurretAllies;
import com.entropy.tacz_turrets.util.TurretEnergyStorage;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.config.common.GunConfig;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.sound.SoundManager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.*;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class TurretEntity extends Mob implements SmartBrainOwner<TurretEntity>, HasTurretInventory, GeoEntity, MenuProvider {
    public static final EntityType<TurretEntity> TYPE = EntityType.Builder.<TurretEntity>of(TurretEntity::new, MobCategory.MISC).sized(1f, 1f).build("turret");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final IGunOperator gunOperator = IGunOperator.fromLivingEntity(this); //LivingEntity is already a gun operator, implementing it here would just be redundant. However, the IDE does not recognize it because it's implemented through a mixin, so this is a small workaround.

    private static final UniformInt ALERT_INTERVAL = TimeUtil.rangeOfSeconds(4, 6);
    private static final int TARGET_SPREAD_INTERVAL = 10;
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
    private final ItemStackHandler inventory = new ItemStackHandler(Math.max(1, TACZTurretsConfig.turretSlotRows * TACZTurretsConfig.turretSlotLength));
    private final List<ItemStack> overflow = new ArrayList<>();
    private final LazyOptional<ItemStackHandler> lazyInventory = LazyOptional.of(() -> inventory);
    private final TurretEnergyStorage energy = new TurretEnergyStorage(TACZTurretsConfig.energyCapacity, TACZTurretsConfig.energyTransferRate);
    private final LazyOptional<TurretEnergyStorage> lazyEnergy = LazyOptional.of(() -> energy);
    public UUID owner;

    public TurretEntity(Level level, BlockPos pos, Player player) {
        this(TYPE, level);
        setPos(pos.getCenter());
        owner = player.getUUID();
        ownerName = player.getGameProfile().getName();
    }

    public TurretEntity(EntityType<? extends TurretEntity> type, Level level) {
        super(type, level);
        gunOperator.initialData();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TurretState.stateName, TurretState.NO_GUN.name);
        entityData.define(RECOIL, 0);
    }

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

    public static AttributeSupplier.@NotNull Builder createLivingAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, Math.max(TACZTurretsConfig.turretRange, TACZTurretsConfig.sniperTurretRange)).add(Attributes.ARMOR, 6.0D).add(Attributes.MAX_HEALTH, TACZTurretsConfig.turretHealth);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Inventory", inventory.serializeNBT());
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
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        resizeInventory();
        if (tag.contains("Owner")) owner = tag.getUUID("Owner");
        enableType = TurretEnableType.byName(tag.getString("EnableType"));
        mode = TurretMode.byName(tag.getString("Mode"));
        playerTargeting = PlayerTargeting.byName(tag.getString("PlayerTargeting"));
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
        return isSniper() ? TACZTurretsConfig.sniperTurretRange : TACZTurretsConfig.turretRange;
    }

    public void tryShoot() {
        if (!isEnabled()) return;
        if (isConservingAmmo() && tickCount - lastShotTick < CONSERVATIVE_SHOT_INTERVAL) return;
        gunOperator.aim(true);
        ShootResult result = shoot();
        switch (result) {
            case SUCCESS -> {
                lastShotTick = tickCount;
                if (TACZTurretsConfig.turretRecoil) entityData.set(RECOIL, RECOIL_TICKS);
                if (TACZTurretsConfig.requireEnergy) energy.consume(TACZTurretsConfig.energyPerShot);
            }
            case NEED_BOLT -> gunOperator.bolt();
            case NO_AMMO -> {
                collectAmmo();
                gunOperator.reload();
            }
            case NOT_DRAW -> gunOperator.draw(this::getGunStack);
        }
        if (TACZTurretsConfig.logTurretShootResults) TACZTurrets.LOGGER.info("Turret shoot result {}", result);
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
        return switch (TACZTurretsConfig.inaccuracyMode) {
            case RANDOM -> (float) TACZTurretsConfig.randomInaccuracy;
            case DISTANCE -> {
                LivingEntity target = BrainUtils.getTargetOfEntity(this);
                if (target == null) yield 0.0F;
                double distance = Math.sqrt(distanceToSqr(target));
                yield (float) (TACZTurretsConfig.distanceInaccuracy * Math.min(1.0D, distance / getRange()));
            }
        };
    }

    public boolean hasAmmo() {
        if (!gunOperator.consumesAmmoOrNot()) return true;
        if (gunHasAmmo()) return true;
        for (int slot = 0; slot < getSlots(); slot++) {
            if (isRightAmmo(getStackInSlot(slot))) {
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
        for (int slot = 0; slot < getSlots(); slot++) {
            if (isCreativeAmmo(getStackInSlot(slot))) {
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

    private <T> LazyOptional<T> getSupplyCapability(BlockEntity blockEntity, Capability<T> capability) {
        LazyOptional<T> sided = blockEntity.getCapability(capability, Direction.UP);
        return sided.isPresent() ? sided : blockEntity.getCapability(capability);
    }

    public void collectAmmo() {
        if (shouldCollectAmmo()) {
            BlockEntity blockEntity = getSupplyBlockEntity();
            if (blockEntity != null) {
                getSupplyCapability(blockEntity, ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                    for (int invSlot = 0; invSlot < getSlots(); invSlot++) {
                        for (int handlerSlot = 0; handlerSlot < handler.getSlots(); handlerSlot++) {
                            ItemStack handlerStack = handler.getStackInSlot(handlerSlot);
                            if (isRightAmmo(handlerStack) && getStackInSlot(invSlot).getCount() < getStackInSlot(invSlot).getMaxStackSize()) {
                                ItemStack remainder = insertItem(invSlot, handler.extractItem(handlerSlot, handlerStack.getCount(), false), false);
                                if (!remainder.isEmpty()) {
                                    handler.insertItem(handlerSlot, remainder, false);
                                }
                            }
                            if (hasCreativeAmmo()) {
                                return;
                            }
                        }
                    }
                });
            }
        }
    }

    public boolean shouldCollectAmmo() {
        return gunOperator.consumesAmmoOrNot() && isEnabled() && !hasCreativeAmmo();
    }

    public boolean hasEnoughEnergy() {
        if (!TACZTurretsConfig.requireEnergy) return true;
        return energy.getEnergyStored() >= Math.max(1, TACZTurretsConfig.energyPerShot);
    }

    private void tickEnergy() {
        if (!TACZTurretsConfig.requireEnergy) return;
        if (TACZTurretsConfig.energyIdleDrain > 0 && !enableType.shouldDisable(level(), blockPosition())) {
            energy.consume(TACZTurretsConfig.energyIdleDrain);
        }
        collectEnergy();
    }

    private void collectEnergy() {
        int space = energy.getMaxEnergyStored() - energy.getEnergyStored();
        if (space <= 0) return;
        BlockEntity blockEntity = getSupplyBlockEntity();
        if (blockEntity == null) return;
        getSupplyCapability(blockEntity, ForgeCapabilities.ENERGY).ifPresent(source -> {
            int available = source.extractEnergy(space, true);
            int accepted = energy.receiveEnergy(available, true);
            if (accepted > 0) energy.receiveEnergy(source.extractEnergy(accepted, false), false);
        });
    }

    private void tickPassiveHealing() {
        if (!TACZTurretsConfig.passiveHealing) return;
        if (getHealth() >= getMaxHealth()) return;
        if (tickCount % TACZTurretsConfig.passiveHealInterval != 0) return;
        heal((float) TACZTurretsConfig.passiveHealAmount);
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
        return distanceToSqr(target) > (double) TACZTurretsConfig.adaptiveRange * TACZTurretsConfig.adaptiveRange;
    }

    private float getRecoilProgress(float partialTick) {
        int recoil = entityData.get(RECOIL);
        if (recoil <= 0) return 0.0F;
        return Mth.clamp((recoil - partialTick) / RECOIL_TICKS, 0.0F, 1.0F);
    }

    public float getRecoilDegrees(float partialTick) {
        if (TACZTurretsConfig.recoilType != RecoilType.BOUNCE) return 0.0F;
        return RECOIL_DEGREES * getRecoilProgress(partialTick);
    }

    public float getRecoilPush(float partialTick) {
        if (TACZTurretsConfig.recoilType != RecoilType.PUSH) return 0.0F;
        return RECOIL_PUSH * getRecoilProgress(partialTick);
    }

    private void onTickServerSide() {
        if (!level().isClientSide()) {
            dropOverflow();
            int recoil = entityData.get(RECOIL);
            if (recoil > 0) entityData.set(RECOIL, recoil - 1);
            tickEnergy();
            tickPassiveHealing();
            if (isEnabled()) {
                if (hasGun()) {
                    ModernKineticGunItem gun = getGunItem();
                    if (!gunDrawn) {
                        gunOperator.draw(this::getGunStack);
                        gunDrawn = true;
                    }
                    ItemStack gunItem = getGunStack();
                    ResourceLocation gunId = gun.getGunId(gunItem);
                    IGun iGun = IGun.getIGunOrNull(gunItem);
                    if (iGun != null) {
                        Optional<CommonGunIndex> gunIndexOptional = TimelessAPI.getCommonGunIndex(gunId);
                        if (gunIndexOptional.isPresent()) {
                            CommonGunIndex gunIndex = gunIndexOptional.get();
                            GunData gunData = gunIndex.getGunData();
                            AttachmentCacheProperty property = new AttachmentCacheProperty();
                            property.eval(getMainHandItem(), gunData);
                        }
                    }

                    if (isEnabled()) {
                        if (gunOperator.getSynReloadState().getStateType().isReloading()) {
                            TurretState.RELOADING.setState(this);
                        } else {
                            if (hasAmmo()) {
                                TurretState.ACTIVE.setState(this);
                            } else {
                                TurretState.NO_AMMO.setState(this);
                                collectAmmo();
                            }
                        }
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
            NetworkHooks.openScreen(serverPlayer, this, buf -> {
                buf.writeVarInt(getId());
                buf.writeByte(layout.rows);
                buf.writeByte(layout.columns);
                buf.writeUtf(getOwnerName());
                Set<UUID> allies = getAllies();
                buf.writeVarInt(allies.size());
                allies.forEach(buf::writeUUID);
                buf.writeBoolean(manages);
            });
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    private InteractionResult manageInteract(Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (!player.isCrouching() && TACZTurretsConfig.repairItems.matches(heldStack)) {
            if (getHealth() < getMaxHealth()) {
                heal((float) TACZTurretsConfig.repairAmount);
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
            for (int slot = 0; slot < getSlots(); slot++) {
                ItemStack slotStack = extractItem(slot, getStackInSlot(slot).getCount(), false);
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
        if (!TACZTurretsConfig.enableSounds) return;
        playSound(sound, 1.0F, 0.9F + random.nextFloat() * 0.2F);
    }

    private void playRepairSound() {
        if (!TACZTurretsConfig.enableSounds) return;
        List<? extends String> sounds = TACZTurretsConfig.repairSounds;
        if (sounds.isEmpty()) return;
        ResourceLocation soundId = ResourceLocation.tryParse(sounds.get(random.nextInt(sounds.size())));
        if (soundId == null) return;
        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(soundId);
        if (sound != null) playSound(sound, 1.0F, 0.9F + random.nextFloat() * 0.2F);
    }

    private void spawnRepairParticles() {
        if (!TACZTurretsConfig.repairParticles) return;
        if (!(level() instanceof ServerLevel serverLevel)) return;
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + getBbHeight() * 0.6D, getZ(), 12, getBbWidth() * 0.5D, getBbHeight() * 0.4D, getBbWidth() * 0.5D, 0.0D);
    }

    private void resizeInventory() {
        int desired = Math.max(1, TACZTurretsConfig.turretSlotRows * TACZTurretsConfig.turretSlotLength);
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
        return isOwnedBy(player) || (TACZTurretsConfig.alliesHavePerms && isAlliedWithOwner(player));
    }

    public boolean isOwnedBy(Player player) {
        return owner == null || player.getUUID().equals(owner) || player.isCreative() || (TACZTurretsConfig.opBypass && player.hasPermissions(2));
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

    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource pSource, int pLooting, boolean pRecentlyHit) {
        for (int i = 0; i < getSlots(); i++) {
            if (!getStackInSlot(i).isEmpty()) spawnAtLocation(extractItem(i, getStackInSlot(i).getCount(), false));
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
                List<TurretEntity> entities = level().getEntitiesOfClass(TurretEntity.class, AABB.ofSize(position(), 64, 16, 64));
                List<TurretEntity> filter1 = entities.stream().filter((e) -> e.hasLineOfSight(entity) || BehaviorUtils.entityIsVisible(e.getBrain(), entity)).toList();
                for (TurretEntity turret : filter1) {
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
        if (!TACZTurretsConfig.turretsTakeDamage) {
            return !source.isCreativePlayer() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        }
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    public void setTarget(@Nullable LivingEntity entity) {
        if (!isEnabled()) return;

        if (getTarget() == null && entity != null) {
            ALERT_INTERVAL.sample(random);
        }

        if (entity instanceof Player) {
            setLastHurtByPlayer((Player) entity);
        }

        super.setTarget(entity);
    }

    protected Brain.@NotNull Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep() {
        if (retaliateTicks < 0 && TACZTurretsConfig.retaliateTargeting != RetaliateTargeting.CLEAR_ON_DEATH) retaliateTicks = retaliationTicks();
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
        if (!isEnabled()) return;

        double range = getRange();
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(range), entity -> !(entity instanceof TurretEntity) && shouldTarget(entity) && hasLineOfSight(entity))) {
            double distance = distanceToSqr(candidate);
            if (distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }
        if (best != null) alertTo(best);
    }

    private void spreadTargets() {
        if (!TACZTurretsConfig.betterTargeting) return;
        if (tickCount % TARGET_SPREAD_INTERVAL != 0) return;

        LivingEntity current = BrainUtils.getTargetOfEntity(this);
        if (current == null) return;

        double range = getRange();
        double currentDistance = distanceToSqr(current);
        double reachSquared = Math.min(range * range, Math.max(currentDistance * TARGET_SPREAD_REACH, TARGET_SPREAD_MIN_RADIUS * TARGET_SPREAD_MIN_RADIUS));
        double reach = Math.sqrt(reachSquared);

        List<TurretEntity> allies = level().getEntitiesOfClass(TurretEntity.class, getBoundingBox().inflate(range), turret -> turret != this);
        int currentClaims = countClaims(allies, current);

        LivingEntity best = current;
        int bestClaims = currentClaims;
        double bestDistance = currentDistance;
        for (LivingEntity candidate : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(reach), entity -> entity != current && !(entity instanceof TurretEntity) && distanceToSqr(entity) <= reachSquared && shouldTarget(entity) && hasLineOfSight(entity))) {
            int claims = countClaims(allies, candidate);
            double distance = distanceToSqr(candidate);
            if (claims < bestClaims || (claims == bestClaims && distance < bestDistance)) {
                best = candidate;
                bestClaims = claims;
                bestDistance = distance;
            }
        }

        if (best != current && (bestClaims < currentClaims || bestDistance < currentDistance * TARGET_SWITCH_MARGIN)) {
            alertTo(best);
        }
    }

    private int countClaims(List<TurretEntity> allies, LivingEntity target) {
        int claims = 0;
        for (TurretEntity ally : allies) {
            if (BrainUtils.getTargetOfEntity(ally) == target) claims++;
        }
        return claims;
    }

    @Override
    public BrainActivityGroup<? extends TurretEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(new Behavior[]{new TargetOrRetaliate<TurretEntity>().isAllyIf((e, l) -> l instanceof TurretEntity).attackablePredicate(l -> l != null && isValidTarget(l) && hasLineOfSight(l)).alertAlliesWhen((m, e) -> e != null && m.hasLineOfSight(e)).runFor((e) -> 999), (new LookAtTarget<>()).runFor((entity) -> RandomSource.create().nextInt(40, 300))});
    }

    public BrainActivityGroup<? extends TurretEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(new Behavior[]{new FirstApplicableBehaviour<TurretEntity>(new TargetOrRetaliate<TurretEntity>().attackablePredicate(l -> l != null && isValidTarget(l) && hasLineOfSight(l)), new SetPlayerLookTarget<>(), new SetRandomLookTarget<>()), (new Idle<>()).runFor((entity) -> RandomSource.create().nextInt(30, 60))});
    }

    public BrainActivityGroup<? extends TurretEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(new Behavior[]{new InvalidateAttackTarget<TurretEntity>().invalidateIf((entity, target) -> !target.isAlive() || (target instanceof Player player && player.getAbilities().invulnerable) || !entity.hasLineOfSight(target) || !entity.isValidTarget(target) || entity.distanceToSqr(target) > entity.getRange() * entity.getRange()).ignoreFailedPathfinding(), new SetRetaliateTarget<>(), new TaczShootAttack<>(TACZTurretsConfig.turretRange).startCondition((x$0) -> getMainHandItem().is(ModItems.MODERN_KINETIC_GUN.get()) && gunOperator.getSynShootCoolDown() == 0)});
    }

    @Override
    public List<? extends ExtendedSensor<? extends TurretEntity>> getSensors() {
        int range = Math.max(TACZTurretsConfig.turretRange, TACZTurretsConfig.sniperTurretRange);
        return ObjectArrayList.of(new NearbyPlayersSensor<TurretEntity>().setRadius(range).setPredicate((p, e) -> e.lastHurtByPlayer != null && p.getUUID().equals(e.lastHurtByPlayer.getUUID())), new HurtBySensor<>(), new NearbyLivingEntitySensor<TurretEntity>().setRadius(range).setPredicate((target, entity) -> shouldTarget(target)));
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
        if (owner != null && level().getServer() != null && TurretAllies.get(level().getServer()).isAlly(owner, target.getUUID())) return true;
        if (!TACZTurretsConfig.respectTeams) return false;
        if (target.getTeam() == null) return false;
        if (isAlliedTo(target)) return true;
        Player ownerPlayer = getOwnerPlayer();
        return ownerPlayer != null && ownerPlayer.isAlliedTo(target);
    }

    private boolean canTargetPlayers() {
        return TACZTurretsConfig.damagePlayers && playerTargeting != PlayerTargeting.NEVER;
    }

    public void markRetaliation(Player player) {
        retaliateTarget = player.getUUID();
        retaliateTicks = TACZTurretsConfig.retaliateTargeting == RetaliateTargeting.CLEAR_ON_DEATH ? RETALIATE_UNTIL_DEATH : retaliationTicks();
    }

    private static int retaliationTicks() {
        return Math.max(1, TACZTurretsConfig.retaliationTimer * 20);
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
        if (distanceToSqr(target) > getRange() * getRange()) return false;
        if (target.getUUID().equals(owner)) return false;
        if (isAlliedWithOwner(target)) return false;

        // Never target entities in the ignore tag or the config blacklist
        if (target.getType().is(TagRegistry.TURRET_IGNORED)) return false;
        if (TACZTurretsConfig.targetBlacklist.matches(target.getType())) return false;

        if (target instanceof Player player) return canTargetPlayers() && !player.isCreative() && !player.isSpectator() && canEngagePlayer(player);
        return true;
    }

    public boolean isProtectedFromFire(LivingEntity victim) {
        if (!TACZTurretsConfig.damagePlayers && victim instanceof Player) return true;
        if (TACZTurretsConfig.ownerTakesNoDamage && victim.getUUID().equals(owner)) return true;
        if (TACZTurretsConfig.alliesCannotBeDamaged && isAlliedWithOwner(victim)) return true;
        return !canDamage(victim);
    }

    public boolean canDamage(LivingEntity victim) {
        if (victim instanceof Player || victim == getTarget() || victim == getLastHurtByMob()) return true;
        TargetFilter filter = TACZTurretsConfig.damageableEntities;
        return filter.isEmpty() ? isTargetableType(victim) : filter.matches(victim.getType());
    }

    private boolean shouldTarget(LivingEntity target) {
        if (!isValidTarget(target)) return false;
        if (target instanceof Player) return true;

        // Direct anger target always passes
        if (target == getTarget()) return true;
        return isTargetableType(target);
    }

    private boolean isTargetableType(LivingEntity target) {
        if (target.getType().is(TagRegistry.TURRET_IGNORED)) return false;
        if (TACZTurretsConfig.targetBlacklist.matches(target.getType())) return false;

        // Entities in the config whitelist or the turret_targets tag are always targeted
        if (TACZTurretsConfig.targetWhitelist.matches(target.getType())) return true;
        if (target.getType().is(TagRegistry.TURRET_TARGETS)) return true;

        // If targetAllMobs is on, target everything that passed the above filters
        if (TACZTurretsConfig.targetAllMobs) return true;

        // Otherwise: vanilla monsters
        if (target instanceof Monster) return true;
        return target.getType().getCategory() == MobCategory.MONSTER;
    }

    @Override
    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return hasGun() && isRightAmmo(stack);
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
    public boolean ignoreExplosion() {
        return true;
    }
}
