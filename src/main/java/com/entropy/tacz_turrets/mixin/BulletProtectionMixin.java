package com.entropy.tacz_turrets.mixin;

import com.entropy.tacz_turrets.turret.TurretEntity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.util.EntityUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(EntityUtil.class)
public class BulletProtectionMixin {
    @ModifyReturnValue(method = "findEntityOnPath", at = @At("RETURN"), remap = false)
    private static EntityKineticBullet.EntityResult turretBulletPassesThrough(@Nullable EntityKineticBullet.EntityResult original, Projectile bulletEntity, Vec3 startVec, Vec3 endVec) {
        if (original == null || !isProtected(bulletEntity, original)) return original;

        EntityKineticBullet.EntityResult closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (EntityKineticBullet.EntityResult candidate : EntityUtil.findEntitiesOnPath(bulletEntity, startVec, endVec)) {
            double distance = startVec.distanceTo(candidate.getHitPos());
            if (distance < closestDistance) {
                closest = candidate;
                closestDistance = distance;
            }
        }
        return closest;
    }

    @ModifyReturnValue(method = "findEntitiesOnPath", at = @At("RETURN"), remap = false)
    private static List<EntityKineticBullet.EntityResult> turretBulletsPassThrough(List<EntityKineticBullet.EntityResult> original, Projectile bulletEntity, Vec3 startVec, Vec3 endVec) {
        if (original.isEmpty()) return original;
        original.removeIf(result -> isProtected(bulletEntity, result));
        return original;
    }

    private static boolean isProtected(Projectile bulletEntity, EntityKineticBullet.EntityResult result) {
        return bulletEntity.getOwner() instanceof TurretEntity turret && result.getEntity() instanceof LivingEntity living && turret.isProtectedFromFire(living);
    }
}
