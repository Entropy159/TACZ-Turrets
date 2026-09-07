package com.entropy.tacz_turrets.turret.ai;

import com.entropy.tacz_turrets.turret.TurretEntity;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
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

    public TaczShootAttack(int radius) {
        super();
        attackRadius = radius;
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull E turret) {
        target = BrainUtils.getTargetOfEntity(turret);
        return turret.isEnabled() && target != null && BrainUtils.canSee(turret, target) && turret.isValidTarget(target);
    }

    @Override
    protected void start(E turret) {
        if (turret.isEnabled() && target != null && BehaviorUtils.entityIsVisible(turret.getBrain(), target)) {
            Vec3 eyePos = target.getEyePosition();
            turret.lookAt(EntityAnchorArgument.Anchor.EYES, eyePos);
            BehaviorUtils.lookAtEntity(turret, target);
            if (turret.hasLineOfSight(target)) {
                if (turret.hasGun()) {
                    turret.tryShoot();
                }
            }
        }
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), Pair.of(MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT)});
    }
}
