package com.entropy.tacz_turrets.client.model;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.client.CreateCogClock;
import com.entropy.tacz_turrets.client.renderer.TurretRenderer;
import com.entropy.tacz_turrets.turret.TurretEntity;
import com.entropy.tacz_turrets.turret.TurretState;
import com.entropy.tacz_turrets.turret.TurretType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector2f;
import software.bernie.geckolib.cache.object.GeoBone;
//? if forge {
import software.bernie.geckolib.core.animation.AnimationState;
//?} else {
/*import software.bernie.geckolib.animation.AnimationState;
*///?}
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class TurretModel extends DefaultedEntityGeoModel<TurretEntity> {
    public TurretModel() {
        super(TACZTurrets.id("turret"));
    }

    @Override
    public ResourceLocation getModelResource(TurretEntity animatable) {
        return animatable.getTurretType().model();
    }

    @Override
    public ResourceLocation getTextureResource(TurretEntity animatable) {
        return animatable.getTurretType().texture(TurretState.getState(animatable));
    }

    @Override
    public void setCustomAnimations(TurretEntity turret, long instanceId, AnimationState<TurretEntity> animationState) {
        var head = getAnimationProcessor().getBone("head");
        Vector2f aim = TurretRenderer.aim(turret, turret.getYHeadRot(), Mth.lerp(animationState.getPartialTick(), turret.xRotO, turret.getXRot()));
        float yaw = 180 - aim.x;
        if (head != null) {
            head.setRotX((-aim.y - turret.getRecoilDegrees(animationState.getPartialTick())) * Mth.DEG_TO_RAD);
            head.setRotY(yaw * Mth.DEG_TO_RAD);
        }
        var center = getAnimationProcessor().getBone("center");
        if (center != null) {
            center.setRotY(yaw * Mth.DEG_TO_RAD);
        }
        var cog = getAnimationProcessor().getBone("cog");
        if (cog != null && TurretType.BRASS.isAvailable()) {
            cog.setRotY(TurretRenderer.up(turret).getAxisDirection().getStep() * CreateCogClock.degrees(turret, animationState.getPartialTick()) * Mth.DEG_TO_RAD);
        }
        if (getAnimationProcessor().getBone("laser") instanceof GeoBone laser) {
            laser.setTrackingMatrices(true);
        }
    }
}
