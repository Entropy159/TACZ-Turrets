package com.entropy.tacz_turrets.client.renderer;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.item.TurretItem;
import com.entropy.tacz_turrets.turret.TurretState;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
//? if forge {
import software.bernie.geckolib.core.animation.AnimationState;
//?} else {
/*import software.bernie.geckolib.animation.AnimationState;
*///?}
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
//? if forge {
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
//?} else {
/*import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
*///?}

public class TurretItemRenderer extends GeoItemRenderer<TurretItem> {
    public static final IClientItemExtensions EXTENSIONS = new IClientItemExtensions() {
        private TurretItemRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (renderer == null) renderer = new TurretItemRenderer();
            return renderer;
        }
    };

    public TurretItemRenderer() {
        super(new GeoModel<>() {
            @Override
            public ResourceLocation getModelResource(TurretItem item) {
                return item.getType().model();
            }

            @Override
            public ResourceLocation getTextureResource(TurretItem item) {
                return item.getType().texture(TurretState.NO_GUN);
            }

            @Override
            public ResourceLocation getAnimationResource(TurretItem item) {
                return TACZTurrets.id("animations/entity/turret.animation.json");
            }

            @Override
            public void setCustomAnimations(TurretItem item, long instanceId, AnimationState<TurretItem> animationState) {
                for (String name : new String[] {"head", "center", "cog"}) {
                    var bone = getAnimationProcessor().getBone(name);
                    if (bone == null) continue;
                    bone.setRotX(bone.getInitialSnapshot().getRotX());
                    bone.setRotY(bone.getInitialSnapshot().getRotY());
                    bone.setRotZ(bone.getInitialSnapshot().getRotZ());
                }
            }
        });
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
