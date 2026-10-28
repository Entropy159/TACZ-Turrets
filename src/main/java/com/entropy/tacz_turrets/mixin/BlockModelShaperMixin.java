package com.entropy.tacz_turrets.mixin;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.util.TurretWorkbench;
import com.tacz.guns.block.AbstractGunSmithTableBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockModelShaper.class)
public class BlockModelShaperMixin {
    @Unique
    private static BlockPos tacz_turrets$lastRoot;

    @Inject(method = "getTexture(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", at = @At("HEAD"), cancellable = true, remap = false)
    private void turretWorkbenchParticles(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<TextureAtlasSprite> cir) {
        if (!(state.getBlock() instanceof AbstractGunSmithTableBlock block)) return;
        BlockPos root = block.getRootPos(pos, state);
        boolean workbench = TurretWorkbench.is(level, pos, state);
        if (workbench) tacz_turrets$lastRoot = root.immutable();
        else workbench = root.equals(tacz_turrets$lastRoot) && level.getBlockEntity(root) == null;
        if (workbench) {
            cir.setReturnValue(Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(TACZTurrets.id("block/turret_workbench_particle")));
        }
    }
}
