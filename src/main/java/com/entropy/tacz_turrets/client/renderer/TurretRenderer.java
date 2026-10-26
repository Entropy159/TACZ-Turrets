package com.entropy.tacz_turrets.client.renderer;

import com.entropy.tacz_turrets.client.model.TurretModel;
import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import com.entropy.tacz_turrets.turret.TurretEntity;
import com.entropy.tacz_turrets.turret.TurretState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.client.model.functional.BeamRenderer;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.client.resource.pojo.display.LaserConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import java.util.List;

public class TurretRenderer extends GeoEntityRenderer<TurretEntity> {
    public TurretRenderer(EntityRendererProvider.Context context) {
        super(context, new TurretModel());
        addRenderLayer(new TurretPaintLayer(this));
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    //? if forge {
    protected void applyRotations(TurretEntity turret, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick) {
        float nativeScale = 1.0F;
    //?} else {
    /*protected void applyRotations(TurretEntity turret, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
    *///?}
        if (isFlipped(turret)) {
            poseStack.translate(0.0F, turret.getBbHeight() / nativeScale, 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
        if (turret.deathTime > 0) {
            float deathRotation = (turret.deathTime + partialTick - 1.0F) / 20.0F * 1.6F;
            poseStack.mulPose(Axis.ZP.rotationDegrees(Math.min(Mth.sqrt(deathRotation), 1.0F) * getDeathMaxRotation(turret)));
        }
    }

    public static boolean isFlipped(TurretEntity turret) {
        return turret.isCeiling() || LivingEntityRenderer.isEntityUpsideDown(turret);
    }

    @Override
    //? if forge {
    public void renderFinal(PoseStack poseStack, TurretEntity turret, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.renderFinal(poseStack, turret, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        poseStack.pushPose();
        applyRotations(turret, poseStack, 0, 0, partialTick);
    //?} else {
    /*public void renderFinal(PoseStack poseStack, TurretEntity turret, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.renderFinal(poseStack, turret, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, colour);
        poseStack.pushPose();
        applyRotations(turret, poseStack, 0, 0, partialTick, turret.getScale());
    *///?}

        float sign = isFlipped(turret) ? -1.0F : 1.0F;
        model.getBone("gun").ifPresent(gun -> poseStack.translate(gun.getModelPosition().x / 16f, gun.getModelPosition().y / 16f, gun.getModelPosition().z / 16f));
        poseStack.mulPose(Axis.YN.rotationDegrees(sign * turret.getYHeadRot() + 180));
        poseStack.mulPose(Axis.XN.rotationDegrees(sign * turret.getXRot() - turret.getRecoilDegrees(partialTick)));
        poseStack.translate(0.0F, 0.0F, turret.getRecoilPush(partialTick));

        if (turret.hasGun()) {
            if (turret.hasMinigun()) {
                poseStack.mulPose(Axis.XN.rotationDegrees(90));
            }
            Minecraft.getInstance().getItemRenderer().renderStatic(turret.getGunStack(), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, packedOverlay, poseStack, bufferSource, turret.level(), 0);
        }

        poseStack.popPose();

        TurretState state = TurretState.getState(turret);
        boolean visible = TACZTurretsConfig.SHOW_OTHERS_LASERS.get() || (Minecraft.getInstance().player != null && turret.isOwnerId(Minecraft.getInstance().player.getUUID()));
        if (visible && turret.isAlive() && turret.hasLaser() && state != TurretState.DISABLED && (state != TurretState.NO_GUN || TACZTurretsConfig.LASER_WITHOUT_GUN.get())) {
            model.getBone("laser").ifPresent(laser -> renderLaser(turret, new Matrix4f(entityRenderTranslations).invert().mul(modelRenderTranslations).mul(laser.getModelSpaceMatrix()), poseStack, partialTick));
        }
    }

    private static void renderLaser(TurretEntity turret, Matrix4f bone, PoseStack poseStack, float partialTick) {
        ItemStack laser = turret.getLaserStack();
        LaserConfig config = TimelessAPI.getClientAttachmentIndex(IAttachment.getIAttachmentOrNull(laser).getAttachmentId(laser)).map(ClientAttachmentIndex::getLaserConfig).orElse(null);
        if (config == null || config.getLengthThird() <= 0) return;

        Vector3f forward = bone.transformDirection(new Vector3f(0, 0, -1));
        Vec3 start = turret.getPosition(partialTick).add(new Vec3(bone.transformPosition(new Vector3f())));
        Vec3 end = start.add(new Vec3(forward).scale(config.getLengthThird() / forward.length()));
        Vec3 stop = turret.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, turret)).getLocation();
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(turret.level(), turret, start, stop, new AABB(start, stop).inflate(1), entity -> !entity.isSpectator() && entity.isPickable());
        if (entityHit != null) stop = entityHit.getLocation();

        poseStack.pushPose();
        poseStack.last().pose().mul(bone);
        poseStack.scale(1, 1, (float) (start.distanceTo(stop) / forward.length()) / config.getLengthThird());
        BeamRenderer.renderLaserBeam(laser, poseStack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, List.of());
        poseStack.popPose();
    }
}
