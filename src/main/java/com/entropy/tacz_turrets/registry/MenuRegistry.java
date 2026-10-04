package com.entropy.tacz_turrets.registry;

import com.entropy.tacz_turrets.menu.TurretMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
//? if forge {
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

import java.util.function.Supplier;

import static com.entropy.tacz_turrets.TACZTurrets.MODID;

public class MenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);

    //? if forge {
    public static final Supplier<MenuType<TurretMenu>> TURRET = MENUS.register("turret", () -> IForgeMenuType.create(TurretMenu::new));
    //?} else {
    /*public static final Supplier<MenuType<TurretMenu>> TURRET = MENUS.register("turret", () -> IMenuTypeExtension.create(TurretMenu::new));
    *///?}
}
