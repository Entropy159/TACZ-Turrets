package com.entropy.tacz_turrets.mixin;

import com.entropy.tacz_turrets.client.DesignatorClient;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void holdDesignator(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        HumanoidArm side = DesignatorClient.holdingArm(entity);
        if (side == null) return;
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;
        ModelPart arm = side == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        arm.xRot = model.head.xRot - Mth.HALF_PI;
        arm.yRot = model.head.yRot;
        arm.zRot = 0.0F;
    }
}
