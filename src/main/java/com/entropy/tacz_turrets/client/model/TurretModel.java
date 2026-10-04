package com.entropy.tacz_turrets.client.model;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import com.entropy.tacz_turrets.turret.TurretEntity;
import com.entropy.tacz_turrets.turret.TurretState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
//? if forge {
import software.bernie.geckolib.core.animation.AnimationState;
//?} else {
/*import software.bernie.geckolib.animation.AnimationState;
*///?}
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class TurretModel extends DefaultedEntityGeoModel<TurretEntity> {
    public TurretModel() {
        super(TACZTurrets.id("turret"));
    }

    @Override
    public ResourceLocation getModelResource(TurretEntity animatable) {
        return TACZTurretsConfig.MODEL_TYPE.get().model();
    }

    @Override
    public ResourceLocation getTextureResource(TurretEntity animatable) {
        return TACZTurretsConfig.MODEL_TYPE.get().texture(TurretState.getState(animatable));
    }

    @Override
    public void setCustomAnimations(TurretEntity turret, long instanceId, AnimationState<TurretEntity> animationState) {
        var head = getAnimationProcessor().getBone("head");
        EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        float yaw = -turret.getYHeadRot() + 180;
        if (head != null) {
            head.setRotX((data.headPitch() - turret.getRecoilDegrees(animationState.getPartialTick())) * Mth.DEG_TO_RAD);
            head.setRotY(yaw * Mth.DEG_TO_RAD);
        }
        var center = getAnimationProcessor().getBone("center");
        if (center != null) {
            center.setRotY(yaw * Mth.DEG_TO_RAD);
        }
    }
}
