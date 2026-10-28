package com.entropy.tacz_turrets.compat;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CreateCompat {
    private static boolean broken;

    public static float cogSpeed(Level level, BlockPos pos) {
        if (broken) return 0.0F;
        try {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos neighbour = pos.relative(side);
                BlockState state = level.getBlockState(neighbour);
                if (state.getBlock() instanceof ICogWheel cog && cog.isSmallCog() && cog.getRotationAxis(state) == Direction.Axis.Y
                        && level.getBlockEntity(neighbour) instanceof KineticBlockEntity kinetic && kinetic.getSpeed() != 0.0F) {
                    return -kinetic.getSpeed();
                }
            }
            return 0.0F;
        } catch (LinkageError error) {
            broken = true;
            return 0.0F;
        }
    }
}
