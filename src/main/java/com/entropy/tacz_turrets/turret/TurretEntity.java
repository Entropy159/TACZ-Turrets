package com.entropy.tacz_turrets.turret;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import com.entropy.tacz_turrets.registry.ItemRegistry;
import com.entropy.tacz_turrets.registry.TagRegistry;
import com.entropy.tacz_turrets.turret.ai.TaczShootAttack;
import com.entropy.tacz_turrets.util.HasTurretInventory;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.item.AmmoItem;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
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
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TurretEntity extends Mob implements SmartBrainOwner<TurretEntity>, HasTurretInventory, GeoEntity {
    public static final EntityType<TurretEntity> TYPE = EntityType.Builder.<TurretEntity>of(TurretEntity::new, MobCategory.MISC).sized(1f, 1f).build("turret");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final IGunOperator gunOperator = IGunOperator.fromLivingEntity(this); //LivingEntity is already a gun operator, implementing it here would just be redundant. However, the IDE does not recognize it because it's implemented through a mixin, so this is a small workaround.

    private static final UniformInt ALERT_INTERVAL = TimeUtil.rangeOfSeconds(4, 6);
    private boolean gunDrawn = false;
    private TurretEnableType enableType = TurretEnableType.ALWAYS_ON;
    private final ItemStackHandler inventory = new ItemStackHandler(5);
    private final LazyOptional<ItemStackHandler> lazyInventory = LazyOptional.of(() -> inventory);
    public UUID owner;

    public TurretEntity(Level level, BlockPos pos, Player player) {
        this(TYPE, level);
        setPos(pos.getCenter());
        owner = player.getUUID();
    }

    public TurretEntity(EntityType<? extends TurretEntity> type, Level level) {
        super(type, level);
        gunOperator.initialData();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TurretState.stateName, TurretState.NO_GUN.name);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyInventory.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyInventory.invalidate();
    }

    public static AttributeSupplier.@NotNull Builder createLivingAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, TACZTurretsConfig.turretRange).add(Attributes.ARMOR, 6.0D).add(Attributes.MAX_HEALTH, TACZTurretsConfig.turretHealth);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Inventory", inventory.serializeNBT());
        if (owner != null) tag.putUUID("Owner", owner);
        tag.putString("EnableType", enableType.name());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        if (tag.contains("Owner")) owner = tag.getUUID("Owner");
        enableType = TurretEnableType.valueOf(tag.getString("EnableType"));
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
        return getGunItem().getCurrentAmmoCount(getGunStack()) > 0;
    }

    public boolean isEnabled() {
        return TurretState.getState(this) != TurretState.DISABLED;
    }

    public void tryShoot() {
        if (!isEnabled()) return;
        gunOperator.aim(true);
        ShootResult result = shoot();
        switch (result) {
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
        return gunOperator.shoot(() -> getViewXRot(1), () -> getViewYRot(1));
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

    public void collectAmmo() {
        if (shouldCollectAmmo()) {
            BlockEntity blockEntity = level().getBlockEntity(blockPosition()) == null ? level().getBlockEntity(blockPosition().below()) : level().getBlockEntity(blockPosition());
            if (blockEntity != null) {
                blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
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

    public void pickUpItem(@NotNull ItemEntity itemEntity) {
        if (!isDeadOrDying() && isEnabled()) {
            ItemStack itemStack = itemEntity.getItem();
            if (wantsToPickUp(itemStack)) {
                if (itemStack.getItem() instanceof ModernKineticGunItem) {
                    if (!hasGun()) {
                        onItemPickup(itemEntity);
                        setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, itemStack);
                        take(itemEntity, 1);
                        itemEntity.discard();
                    }
                } else if (hasGun() && isRightAmmo(itemStack)) {
                    for (int slot = 0; slot < getSlots(); slot++) {
                        if (getStackInSlot(slot).isEmpty() || (getStackInSlot(slot).is(itemStack.getItem()))) {
                            onItemPickup(itemEntity);
                            int count = Math.min(itemStack.getCount(), getStackInSlot(slot).getMaxStackSize() - getStackInSlot(slot).getCount());
                            setStackInSlot(slot, itemStack.copyWithCount(count + getStackInSlot(slot).getCount()));
                            if (count >= itemStack.getCount()) {
                                take(itemEntity, count);
                                itemEntity.discard();
                                return;
                            } else if (count > 0) {
                                take(itemEntity, count);
                                itemEntity.setItem(itemStack.copyWithCount(itemStack.getCount() - count));
                                itemStack = itemEntity.getItem();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (getTarget() != null && !getTarget().isAlive()) {
            setTarget(null);
        }
        onTickServerSide();

        if (hasGun() && !gunHasAmmo() && !gunOperator.getSynReloadState().getStateType().isReloading()) {
            gunOperator.reload();
        }

        List<ItemEntity> items = level().getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(1.1));
        if (!items.isEmpty() && !level().isClientSide()) {
            for (ItemEntity item : items) {
                if ((item.getItem().getItem() instanceof ModernKineticGunItem && !hasGun()) || (item.getItem().getItem() instanceof AmmoItem ammo && ammo.isAmmoOfGun(getGunStack(), item.getItem()) && TACZTurretsConfig.consumeAmmo)) {
                    pickUpItem(item);
                }
            }
        }
    }

    private void onTickServerSide() {
        if (!level().isClientSide()) {
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
        return enableType.shouldDisable(level(), blockPosition());
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!player.getUUID().equals(owner)) {
            return super.mobInteract(player, hand);
        }
        if (!getGunStack().isEmpty() || player.isCrouching()) {
            player.getInventory().add(getGunStack());
            setGunStack(ItemStack.EMPTY);
            for (int slot = 0; slot < getSlots(); slot++) {
                if (!getStackInSlot(slot).isEmpty()) {
                    if (!player.getInventory().add(extractItem(slot, getStackInSlot(slot).getCount(), false))) {
                        spawnAtLocation(extractItem(slot, getStackInSlot(slot).getCount(), false));
                    }
                }
            }
            if (player.isCrouching()) {
                if (!player.isCreative()) {
                    player.getInventory().add(new ItemStack(ItemRegistry.TURRET.get()));
                }
                discard();
            }
            return InteractionResult.SUCCESS;
        } else if (player.getItemInHand(hand).is(ModItems.MODERN_KINETIC_GUN.get())) {
            setGunStack(player.getItemInHand(hand));
            player.setItemInHand(hand, ItemStack.EMPTY);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource pSource, int pLooting, boolean pRecentlyHit) {
        for (int i = 0; i < getSlots() - 1; i++) {
            if (!getStackInSlot(i).isEmpty()) spawnAtLocation(extractItem(i, getStackInSlot(i).getCount(), false));
        }
        if (hasGun()) spawnAtLocation(getGunStack());
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (source.getEntity() instanceof TurretEntity) {
            return false;
        }
        if (source.getEntity() instanceof LivingEntity entity && !entity.getUUID().equals(owner)) {
            setTarget(entity);
            List<TurretEntity> entities = level().getEntitiesOfClass(TurretEntity.class, AABB.ofSize(position(), 64, 16, 64));
            List<TurretEntity> filter1 = entities.stream().filter((e) -> e.hasLineOfSight(entity) || BehaviorUtils.entityIsVisible(e.getBrain(), entity)).toList();
            for (TurretEntity turret : filter1) {
                turret.setTarget(entity);
                turret.brain.setMemory(MemoryModuleType.ATTACK_TARGET, entity);
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
        tickBrain(this);
    }

    @Override
    public BrainActivityGroup<? extends TurretEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(new Behavior[]{new TargetOrRetaliate<TurretEntity>().isAllyIf((e, l) -> l instanceof TurretEntity).attackablePredicate(l -> l != null && hasLineOfSight(l)).alertAlliesWhen((m, e) -> e != null && m.hasLineOfSight(e)).runFor((e) -> 999), (new LookAtTarget<>()).runFor((entity) -> RandomSource.create().nextInt(40, 300))});
    }

    public BrainActivityGroup<? extends TurretEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(new Behavior[]{new FirstApplicableBehaviour<TurretEntity>(new TargetOrRetaliate<>(), new SetPlayerLookTarget<>(), new SetRandomLookTarget<>()), (new Idle<>()).runFor((entity) -> RandomSource.create().nextInt(30, 60))});
    }

    public BrainActivityGroup<? extends TurretEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(new Behavior[]{new InvalidateAttackTarget<>().invalidateIf((entity, target) -> !target.isAlive() || (target instanceof Player player && player.getAbilities().invulnerable) || !entity.hasLineOfSight(target) || entity.distanceToSqr(target) > TACZTurretsConfig.turretRange * TACZTurretsConfig.turretRange).ignoreFailedPathfinding(), new SetRetaliateTarget<>(), new TaczShootAttack<>(TACZTurretsConfig.turretRange).startCondition((x$0) -> getMainHandItem().is(ModItems.MODERN_KINETIC_GUN.get()) && gunOperator.getSynShootCoolDown() == 0)});
    }

    @Override
    public List<? extends ExtendedSensor<? extends TurretEntity>> getSensors() {
        int range = TACZTurretsConfig.turretRange > 0 ? TACZTurretsConfig.turretRange : 64;
        return ObjectArrayList.of(new NearbyPlayersSensor<TurretEntity>().setRadius(range).setPredicate((p, e) -> e.lastHurtByPlayer != null && p.getUUID().equals(e.lastHurtByPlayer.getUUID())), new HurtBySensor<>(), new NearbyLivingEntitySensor<TurretEntity>().setRadius(range).setPredicate((target, entity) -> shouldTarget(target)));
    }

    private boolean shouldTarget(LivingEntity target) {
        if (!isEnabled()) return false;
        if (target == this || !target.isAlive()) return false;
        if (target instanceof TurretEntity) return false;
        if (target.getUUID().equals(owner)) return false;
        if (target instanceof Player) return target.equals(lastHurtByPlayer);

        // Never target entities in the ignore tag
        if (target.getType().is(TagRegistry.TURRET_IGNORED)) return false;

        // Direct anger target always passes
        if (target == getTarget()) return true;

        // If targetAllMobs is on, target everything that passed the above filters
        if (TACZTurretsConfig.targetAllMobs) return true;

        // Otherwise: vanilla monsters
        if (target instanceof Monster) return true;
        if (target.getType().getCategory() == MobCategory.MONSTER) return true;
        // Or entities in the turret_targets tag
        return target.getType().is(TagRegistry.TURRET_TARGETS);
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
