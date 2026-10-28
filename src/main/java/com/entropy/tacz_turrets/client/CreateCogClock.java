package com.entropy.tacz_turrets.client;

import com.entropy.tacz_turrets.turret.TurretEntity;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class CreateCogClock {
    public static float degrees(TurretEntity turret, float partialTick) {
        float time = VisualizationManager.supportsVisualization(turret.level()) ? Minecraft.getInstance().levelRenderer.getTicks() + partialTick : AnimationTickHolder.getRenderTime(turret.level());
        BlockPos pos = turret.blockPosition();
        float offset = (pos.getX() + pos.getY() + pos.getZ() - pos.get(turret.getAttachFace().getAxis())) % 2 == 0 ? 22.5F : 0.0F;
        return (time * turret.getRotationSpeed() * 3.0F / 10.0F + offset) % 360.0F;
    }
}
