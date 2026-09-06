package com.entropy.tacz_turrets.registry;

import com.entropy.tacz_turrets.menu.TurretMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.entropy.tacz_turrets.TACZTurrets.MODID;

public class MenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    public static final RegistryObject<MenuType<TurretMenu>> TURRET = MENUS.register("turret", () -> IForgeMenuType.create(TurretMenu::new));
}
