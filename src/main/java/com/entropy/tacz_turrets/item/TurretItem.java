package com.entropy.tacz_turrets.item;

import com.entropy.tacz_turrets.registry.SoundRegistry;
import com.entropy.tacz_turrets.turret.TurretEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public class TurretItem extends Item {
    public TurretItem() {
        super(new Item.Properties());
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null) return InteractionResult.PASS;
        if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        TurretEntity turret = new TurretEntity(context.getLevel(), context.getClickedPos().relative(context.getClickedFace()), context.getPlayer());
        context.getLevel().addFreshEntity(turret);
        turret.playTurretSound(SoundRegistry.TURRET_PLACE.get());
        if (!context.getPlayer().isCreative()) context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
}
