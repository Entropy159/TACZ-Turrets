package com.entropy.tacz_turrets.client;

import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import com.entropy.tacz_turrets.item.LaserDesignatorItem;
import com.entropy.tacz_turrets.network.DesignatorFirePacket;
import com.entropy.tacz_turrets.network.TACZTurretsNetwork;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.model.functional.BeamRenderer;
import com.tacz.guns.client.resource.pojo.display.LaserConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
//? if forge {
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
//?} else {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
*///?}

import java.util.List;

import static com.entropy.tacz_turrets.TACZTurrets.MODID;

//? if forge {
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID)
//?} else {
/*@EventBusSubscriber(value = Dist.CLIENT, modid = MODID)
*///?}
public class DesignatorClient {
    private static final float BEAM_LENGTH = new LaserConfig().getLengthThird();
    private static final DustParticleOptions DOT = new DustParticleOptions(new Vector3f(1.0F, 0.1F, 0.1F), 0.6F);
    private static final Vec3 FIRST_PERSON_LENS = new Vec3(0.4115D, -0.1763D, -1.1724D);
    private static final double HAND_FOV = Math.tan(Math.toRadians(35.0D));
    private static boolean firing = false;

    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        Player player = Minecraft.getInstance().player;
        if (event.isAttack() && player != null && LaserDesignatorItem.isActive(player.getMainHandItem())) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    @SubscribeEvent
    //? if forge {
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
    //?} else {
    /*public static void onClientTick(ClientTickEvent.Post event) {
    *///?}
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            firing = false;
            return;
        }
        boolean held = minecraft.screen == null && minecraft.options.keyAttack.isDown() && LaserDesignatorItem.isActive(minecraft.player.getMainHandItem());
        if (held != firing) {
            firing = held;
            TACZTurretsNetwork.sendToServer(new DesignatorFirePacket(held));
        }
        if (minecraft.isPaused()) return;
        for (Player player : minecraft.level.players()) {
            if (!visible(minecraft, player)) continue;
            Vec3 dot = LaserDesignatorItem.aim(player, 1.0F).getLocation().subtract(player.getViewVector(1.0F).scale(0.05D));
            minecraft.level.addParticle(DOT, dot.x, dot.y, dot.z, 0.0D, 0.0D, 0.0D);
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        //? if forge {
        float partialTick = event.getPartialTick();
        //?} else {
        /*float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        *///?}
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        boolean drawn = false;
        for (Player player : minecraft.level.players()) {
            if (!visible(minecraft, player)) continue;
            InteractionHand hand = LaserDesignatorItem.activeHand(player);
            Vec3 start = beamStart(minecraft, player, hand, partialTick);
            Vec3 beam = LaserDesignatorItem.aim(player, partialTick).getLocation().subtract(start);
            double length = beam.length();
            if (length < 0.1D) continue;
            poseStack.pushPose();
            poseStack.translate(start.x - camera.x, start.y - camera.y, start.z - camera.z);
            poseStack.mulPose(new Quaternionf().rotationTo(new Vector3f(0.0F, 0.0F, -1.0F), beam.toVector3f().normalize()));
            poseStack.scale(1.0F, 1.0F, (float) length / BEAM_LENGTH);
            BeamRenderer.renderLaserBeam(player.getItemInHand(hand), poseStack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, List.of(), buffers);
            poseStack.popPose();
            drawn = true;
        }
        if (drawn) buffers.endBatch();
    }

    private static boolean visible(Minecraft minecraft, Player player) {
        return LaserDesignatorItem.activeHand(player) != null && (player == minecraft.player || TACZTurretsConfig.SHOW_OTHERS_LASERS.get());
    }

    @Nullable
    public static HumanoidArm holdingArm(LivingEntity entity) {
        if (entity.getMainHandItem().getItem() instanceof LaserDesignatorItem) return entity.getMainArm();
        if (entity.getOffhandItem().getItem() instanceof LaserDesignatorItem) return entity.getMainArm().getOpposite();
        return null;
    }

    public static boolean isPressed(@Nullable LivingEntity entity) {
        return firing && entity == Minecraft.getInstance().player;
    }

    private static Vec3 beamStart(Minecraft minecraft, Player player, InteractionHand hand, float partialTick) {
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        double side = arm == HumanoidArm.RIGHT ? 1.0D : -1.0D;
        Vec3 look = player.getViewVector(partialTick);
        if (player == minecraft.getCameraEntity() && minecraft.options.getCameraType().isFirstPerson()) {
            Vec3 right = new Vec3(-look.z, 0.0D, look.x).normalize();
            Vec3 up = right.cross(look);
            double fov = Math.tan(Math.toRadians(minecraft.options.fov().get() / 2.0D)) / HAND_FOV;
            return player.getEyePosition(partialTick).add(look.scale(-FIRST_PERSON_LENS.z)).add(right.scale(FIRST_PERSON_LENS.x * side * fov)).add(up.scale(FIRST_PERSON_LENS.y * fov));
        }
        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
        float headYaw = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
        float pitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());
        Vec3 right = new Vec3(-Mth.cos(bodyYaw), 0.0D, -Mth.sin(bodyYaw));
        Vec3 reach = Vec3.directionFromRotation(pitch, headYaw);
        Vec3 armUp = Vec3.directionFromRotation(pitch - 90.0F, headYaw);
        Vec3 shoulder = player.getPosition(partialTick).add(0.0D, player.isCrouching() ? 0.98D : 1.29D, 0.0D).add(right.scale(0.29D * side));
        return shoulder.add(reach.scale(0.84D)).add(armUp.scale(0.16D));
    }
}
