package com.entropy.tacz_turrets.mixin;

import com.entropy.tacz_turrets.registry.TagRegistry;
import com.entropy.tacz_turrets.turret.TurretEntity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DamageSource.class)
public class DeathMessageMixin {
    @ModifyReturnValue(method = "getLocalizedDeathMessage", at = @At("RETURN"))
    private Component turretDeathMessage(Component original, LivingEntity killed) {
        DamageSource source = (DamageSource) (Object) this;
        if (!(source.getEntity() instanceof TurretEntity turret) || !source.is(TagRegistry.TACZ_BULLETS)) return original;
        return Component.translatable("death.attack.tacz_turrets.turret." + (killed.getRandom().nextInt(TurretEntity.DEATH_MESSAGES) + 1), killed.getDisplayName(), turret.getDisplayName());
    }
}
