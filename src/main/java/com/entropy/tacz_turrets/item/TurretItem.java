package com.entropy.tacz_turrets.item;

import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import com.entropy.tacz_turrets.registry.SoundRegistry;
import com.entropy.tacz_turrets.turret.TurretEntity;
import com.entropy.tacz_turrets.turret.TurretType;
import com.entropy.tacz_turrets.util.PlacedTurrets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.util.GeckoLibUtil;
//? if forge {
import com.entropy.tacz_turrets.client.renderer.TurretItemRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.function.Consumer;
//?} else {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
*///?}

public class TurretItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final TurretType type;

    public TurretItem(TurretType type) {
        super(new Item.Properties());
        this.type = type;
    }

    public TurretType getType() {
        return type;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    //? if forge {
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(TurretItemRenderer.EXTENSIONS);
    }
    //?}

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        Direction attach = context.getClickedFace().getOpposite();
        if (turretInTheWay(level, pos, attach)) return InteractionResult.FAIL;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!TACZTurretsConfig.enabled(type)) {
            context.getPlayer().displayClientMessage(Component.translatable("message.tacz_turrets.turret_disabled"), true);
            return InteractionResult.FAIL;
        }
        int limit = TACZTurretsConfig.MAX_TURRETS_PER_PLAYER.get();
        if (limit > 0 && !context.getPlayer().isCreative() && level.getServer() != null && PlacedTurrets.get(level.getServer()).count(context.getPlayer().getUUID()) >= limit) {
            context.getPlayer().displayClientMessage(Component.translatable("message.tacz_turrets.turret_limit", limit), true);
            return InteractionResult.FAIL;
        }
        TurretEntity turret = new TurretEntity(level, pos, context.getPlayer(), type);
        turret.setAttachFace(attach);
        //? if forge {
        if (context.getItemInHand().getTag() != null) turret.loadPaint(context.getItemInHand().getTag());
        //?} else {
        /*turret.loadPaint(context.getItemInHand().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        *///?}
        level.addFreshEntity(turret);
        turret.trackPlacement();
        turret.playTurretSound(SoundRegistry.TURRET_PLACE.get());
        if (!context.getPlayer().isCreative()) context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }

    private static boolean turretInTheWay(Level level, BlockPos pos, Direction attach) {
        BlockPos landing = pos;
        while (attach == Direction.DOWN && landing.getY() > level.getMinBuildHeight() && level.getBlockState(landing.below()).getCollisionShape(level, landing.below()).isEmpty()) {
            landing = landing.below();
        }
        return !level.getEntitiesOfClass(TurretEntity.class, new AABB(landing).minmax(new AABB(pos))).isEmpty();
    }
}
