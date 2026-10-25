package com.entropy.tacz_turrets.util;

import com.entropy.tacz_turrets.TACZTurrets;
import com.tacz.guns.block.AbstractGunSmithTableBlock;
import com.tacz.guns.block.entity.GunSmithTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class TurretWorkbench {
    public static boolean is(BlockGetter level, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof AbstractGunSmithTableBlock block && level.getBlockEntity(block.getRootPos(pos, state)) instanceof GunSmithTableBlockEntity table && TACZTurrets.TURRET_WORKBENCH.equals(table.getId());
    }
}
