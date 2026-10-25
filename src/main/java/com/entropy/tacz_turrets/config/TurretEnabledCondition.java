package com.entropy.tacz_turrets.config;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.turret.TurretType;
import com.entropy.tacz_turrets.util.Enums;
import net.minecraft.resources.ResourceLocation;
//? if forge {
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
//?} else {
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;
*///?}

import java.util.Locale;
import java.util.Optional;

public record TurretEnabledCondition(TurretType type, Optional<Boolean> identical) implements ICondition {
    public static final ResourceLocation ID = TACZTurrets.id("turret_enabled");

    private static TurretEnabledCondition of(String turret, Optional<Boolean> identical) {
        return new TurretEnabledCondition(Enums.byName(turret.toUpperCase(Locale.ROOT), TurretType.MODERN), identical);
    }

    @Override
    public boolean test(IContext context) {
        return TACZTurretsConfig.enabled(type) && identical.map(value -> value == TACZTurretsConfig.IDENTICAL_TURRETS.get()).orElse(true);
    }

    //? if forge {
    public static final IConditionSerializer<TurretEnabledCondition> SERIALIZER = new IConditionSerializer<>() {
        @Override
        public void write(JsonObject json, TurretEnabledCondition condition) {
            json.addProperty("turret", condition.type.id());
            condition.identical.ifPresent(value -> json.addProperty("identical", value));
        }

        @Override
        public TurretEnabledCondition read(JsonObject json) {
            return of(GsonHelper.getAsString(json, "turret"), json.has("identical") ? Optional.of(GsonHelper.getAsBoolean(json, "identical")) : Optional.empty());
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    };

    @Override
    public ResourceLocation getID() {
        return ID;
    }
    //?} else {
    /*public static final MapCodec<TurretEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("turret").forGetter(condition -> condition.type.id()),
            Codec.BOOL.optionalFieldOf("identical").forGetter(TurretEnabledCondition::identical)
    ).apply(instance, TurretEnabledCondition::of));

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
    *///?}
}
