package com.entropy.tacz_turrets.item;

import com.entropy.tacz_turrets.TACZTurrets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class CamoItem extends Item {
    private final ResourceLocation pattern;

    public CamoItem(String name) {
        super(new Item.Properties());
        pattern = TACZTurrets.id("textures/entity/camo/" + name + ".png");
    }

    public ResourceLocation pattern() {
        return pattern;
    }
}
