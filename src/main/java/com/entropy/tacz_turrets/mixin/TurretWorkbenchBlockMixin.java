package com.entropy.tacz_turrets.mixin;

import com.entropy.tacz_turrets.util.TurretWorkbench;
import com.tacz.guns.block.AbstractGunSmithTableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractGunSmithTableBlock.class)
public abstract class TurretWorkbenchBlockMixin extends BaseEntityBlock {
    private TurretWorkbenchBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    //? if forge {
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
    //?} else {
    /*protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
    *///?}
        if (!TurretWorkbench.is(level, pos, state)) return super.getDestroyProgress(state, player, level, pos);
        return player.getDestroySpeed(Blocks.IRON_BLOCK.defaultBlockState()) / 5.0F / 30;
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return TurretWorkbench.is(level, pos, state) ? SoundType.METAL : soundType;
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return TurretWorkbench.is(level, pos, state) ? 6.0F : getExplosionResistance();
    }
}
