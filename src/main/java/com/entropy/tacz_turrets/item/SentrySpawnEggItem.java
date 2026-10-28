package com.entropy.tacz_turrets.item;

import com.entropy.tacz_turrets.registry.EntityTypeRegistry;
import com.entropy.tacz_turrets.turret.TurretEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
//? if forge {
import net.minecraftforge.common.ForgeSpawnEggItem;

public class SentrySpawnEggItem extends ForgeSpawnEggItem {
//?} else {
/*import net.neoforged.neoforge.common.DeferredSpawnEggItem;

public class SentrySpawnEggItem extends DeferredSpawnEggItem {
*///?}
    public SentrySpawnEggItem() {
        super(EntityTypeRegistry.SENTRY, 0x3B4046, 0xD8492F, new Item.Properties());
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Direction attach = context.getClickedFace().getOpposite();
        BlockPos clicked = context.getClickedPos();
        BlockState state = context.getLevel().getBlockState(clicked);
        if (attach == Direction.DOWN || state.is(Blocks.SPAWNER) || state.getCollisionShape(context.getLevel(), clicked).isEmpty()) return super.useOn(context);
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        TurretEntity sentry = EntityTypeRegistry.SENTRY.get().spawn(level, context.getItemInHand(), context.getPlayer(), clicked.relative(context.getClickedFace()), MobSpawnType.SPAWN_EGG, false, false);
        if (sentry != null) {
            sentry.setAttachFace(attach);
            context.getItemInHand().shrink(1);
            level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, clicked);
        }
        return InteractionResult.CONSUME;
    }
}
