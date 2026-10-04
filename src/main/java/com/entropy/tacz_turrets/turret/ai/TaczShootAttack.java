package com.entropy.tacz_turrets.turret.ai;

import com.entropy.tacz_turrets.turret.TurretEntity;
import com.mojang.datafixers.util.Pair;
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
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = List.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), Pair.of(MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT));
    private @Nullable LivingEntity target;

    @Override
    public List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull E turret) {
        target = BrainUtils.getTargetOfEntity(turret);
        return turret.isEnabled() && target != null && BrainUtils.canSee(turret, target) && turret.isValidTarget(target);
    }

    @Override
    protected void start(E turret) {
        if (target == null || !BehaviorUtils.entityIsVisible(turret.getBrain(), target)) return;
        turret.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        BehaviorUtils.lookAtEntity(turret, target);
        if (turret.hasGun() && turret.getSensing().hasLineOfSight(target)) turret.tryShoot();
    }
}
