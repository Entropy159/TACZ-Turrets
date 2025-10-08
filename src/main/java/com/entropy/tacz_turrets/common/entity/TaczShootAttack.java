package com.entropy.tacz_turrets.common.entity;

import com.mojang.datafixers.util.Pair;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.item.ModernKineticGunItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TaczShootAttack<E extends TurretEntity> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;
    protected float attackRadius;
    protected @Nullable LivingEntity target = null;

    public List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    public TaczShootAttack(int attackRadius) {
        super();
        this.attackRadius = attackRadius;
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull E entity) {
        this.target = BrainUtils.getTargetOfEntity(entity);
        return target != null && BrainUtils.canSee(entity, this.target) && target.getUUID() != entity.owner;
    }

    @Override
    protected void start(E entity) {
        if (this.target != null && entity.getTarget() != null && BehaviorUtils.entityIsVisible(entity.getBrain(), entity.getTarget())) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, entity.getTarget().getPosition(1f));
            BehaviorUtils.lookAtEntity(entity, entity.getTarget());
            if (entity.hasLineOfSight(entity.getTarget())) {
                if (entity.getMainHandItem().getItem() instanceof ModernKineticGunItem) {
                    entity.aim(true);
                    ShootResult result = entity.shoot(() -> entity.getViewXRot(1f), () -> entity.getViewYRot(1f));
                    switch (result) {
                        case SUCCESS -> {
                            entity.firing = true;
                            entity.collectiveShots++;
                            entity.rangedCooldown = entity.getStateRangedCooldown();
                        }
                        case NEED_BOLT -> entity.bolt();
                        case NO_AMMO -> entity.reload();
                    }
                    //BrainUtils.setForgettableMemory(entity, MemoryModuleType.ATTACK_COOLING_DOWN, true, (Integer) 1);
                }
            }
        }
    }

    @Override
    protected void stop(E entity) {
        //super.stop(entity);
    }

    @Override
    protected void tick(E entity) {
        super.tick(entity);
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), Pair.of(MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT)});
    }
}
