package com.entropy.tacz_turrets.client.renderer;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.common.entity.TurretEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class TurretModel extends GeoModel<TurretEntity> {
    @Override
    public ResourceLocation getModelResource(TurretEntity turretEntity) {
        return new ResourceLocation(TACZTurrets.MODID, "geo/entity/turret.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TurretEntity animatable) {
        return TurretEntity.TurretState.getState(animatable).getPath();
    }

    @Override
    public ResourceLocation getAnimationResource(TurretEntity turretEntity) {
        return new ResourceLocation(TACZTurrets.MODID, "animations/entity/turret.animation.json");
    }

    @Override
    public void setCustomAnimations(TurretEntity turret, long instanceId, AnimationState<TurretEntity> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");
        EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        float yaw = -turret.getYHeadRot() + 180;
        if (head != null) {
            head.setRotX(data.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(yaw * Mth.DEG_TO_RAD);
        }
        CoreGeoBone center = getAnimationProcessor().getBone("center");
        if (center != null) {
            center.setRotY(yaw * Mth.DEG_TO_RAD);
        }
    }
}
