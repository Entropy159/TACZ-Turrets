package com.entropy.tacz_turrets.client.renderer;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.TurretItem;
import com.entropy.tacz_turrets.common.entity.TurretEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TurretItemModel extends GeoModel<TurretItem> {
    @Override
    public ResourceLocation getModelResource(TurretItem turretItem) {
        return new ResourceLocation(TACZTurrets.MODID, "geo/entity/turret.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TurretItem turretItem) {
        return TurretEntity.TurretState.NO_GUN.getPath();
    }

    @Override
    public ResourceLocation getAnimationResource(TurretItem turretItem) {
        return new ResourceLocation(TACZTurrets.MODID, "animations/entity/turret.animation.json");
    }
}
