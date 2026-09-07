package com.entropy.tacz_turrets.client.model;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.turret.TurretEntity;
import com.entropy.tacz_turrets.item.TurretItem;
import com.entropy.tacz_turrets.turret.TurretState;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class TurretItemModel extends GeoModel<TurretItem> {
    @Override
    public ResourceLocation getModelResource(TurretItem turretItem) {
        return TACZTurrets.id("geo/entity/turret.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TurretItem turretItem) {
        return TurretState.NO_GUN.getPath();
    }

    @Override
    public ResourceLocation getAnimationResource(TurretItem turretItem) {
        return TACZTurrets.id("animations/entity/turret.animation.json");
    }

    @Override
    public void setCustomAnimations(TurretItem turretItem, long instanceId, AnimationState<TurretItem> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");
        if (head != null) {
            head.setRotX(0);
            head.setRotY(0);
        }
        CoreGeoBone center = getAnimationProcessor().getBone("center");
        if (center != null) {
            center.setRotY(0);
        }
    }
}
