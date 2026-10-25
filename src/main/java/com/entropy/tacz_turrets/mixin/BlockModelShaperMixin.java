package com.entropy.tacz_turrets.mixin;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.util.TurretWorkbench;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockModelShaper.class)
public class BlockModelShaperMixin {
    @Inject(method = "getTexture(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", at = @At("HEAD"), cancellable = true, remap = false)
    private void turretWorkbenchParticles(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<TextureAtlasSprite> cir) {
        if (TurretWorkbench.is(level, pos, state)) {
            cir.setReturnValue(Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(TACZTurrets.id("block/turret_workbench_particle")));
        }
    }
}
