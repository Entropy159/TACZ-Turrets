package com.entropy.tacz_turrets.event;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import com.entropy.tacz_turrets.turret.RetaliateTargeting;
import com.entropy.tacz_turrets.turret.TurretEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber(modid = TACZTurrets.MODID)
public class TurretEventHandler {
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        TurretEntity turret = getAttackingTurret(event.getSource());
        if (turret == null) return;
        if (turret.isProtectedFromFire(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        TurretEntity turret = getAttackingTurret(event.getSource());
        if (turret != null) {
            if (TACZTurretsConfig.creditKillsToOwner) {
                Player owner = turret.getOwnerPlayer();
                if (owner != null && owner != victim) victim.setLastHurtByPlayer(owner);
            }
            return;
        }

        if (TACZTurretsConfig.protectOwner && victim instanceof Player owner && event.getSource().getEntity() instanceof LivingEntity attacker) {
            defendOwner(owner, attacker);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (TACZTurretsConfig.retaliateTargeting != RetaliateTargeting.CLEAR_ON_DEATH) return;
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) return;

        double range = Math.max(TACZTurretsConfig.turretRange, TACZTurretsConfig.sniperTurretRange);
        for (TurretEntity turret : player.level().getEntitiesOfClass(TurretEntity.class, AABB.ofSize(player.position(), range * 2, range * 2, range * 2))) {
            turret.forgetRetaliation(player.getUUID());
        }
    }

    @Nullable
    private static TurretEntity getAttackingTurret(DamageSource source) {
        if (source.getEntity() instanceof TurretEntity turret) return turret;
        if (source.getDirectEntity() instanceof TurretEntity turret) return turret;
        return null;
    }

    private static void defendOwner(Player owner, LivingEntity attacker) {
        if (attacker == owner) return;
        double range = TACZTurretsConfig.turretRange;
        List<TurretEntity> turrets = owner.level().getEntitiesOfClass(TurretEntity.class, AABB.ofSize(owner.position(), range * 2, range, range * 2), turret -> owner.getUUID().equals(turret.owner));
        for (TurretEntity turret : turrets) {
            if (attacker instanceof Player player) turret.markRetaliation(player);
            if (turret.isValidTarget(attacker)) turret.alertTo(attacker);
        }
    }
}
