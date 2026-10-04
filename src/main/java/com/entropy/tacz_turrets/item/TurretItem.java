package com.entropy.tacz_turrets.item;

import com.entropy.tacz_turrets.registry.SoundRegistry;
import com.entropy.tacz_turrets.turret.TurretEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class TurretItem extends Item {
    public TurretItem() {
        super(new Item.Properties());
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        boolean ceiling = context.getClickedFace() == Direction.DOWN;
        if (turretInTheWay(level, pos, ceiling)) return InteractionResult.FAIL;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        TurretEntity turret = new TurretEntity(level, pos, context.getPlayer());
        turret.setCeiling(ceiling);
        level.addFreshEntity(turret);
        turret.playTurretSound(SoundRegistry.TURRET_PLACE.get());
        if (!context.getPlayer().isCreative()) context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }

    private static boolean turretInTheWay(Level level, BlockPos pos, boolean ceiling) {
        BlockPos landing = pos;
        while (!ceiling && landing.getY() > level.getMinBuildHeight() && level.getBlockState(landing.below()).getCollisionShape(level, landing.below()).isEmpty()) {
            landing = landing.below();
        }
        return !level.getEntitiesOfClass(TurretEntity.class, new AABB(landing).minmax(new AABB(pos))).isEmpty();
    }
}
