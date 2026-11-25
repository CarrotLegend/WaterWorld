package com.carrot123.waterworld.registry;

import com.carrot123.waterworld.menu.SeawaterFilterMenu;
import com.carrot123.waterworld.menu.OxygenTankMenu;
import com.carrot123.waterworld.menu.OxygenMachineMenu;
import com.carrot123.waterworld.oxygen.OxygenMachineType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModMenus {
    public static final RegistryObject<MenuType<OxygenTankMenu>> OXYGEN_TANKS =
            RegistryObject.create(ResourceLocation.fromNamespaceAndPath("waterworld", "oxygen_tanks"), Registries.MENU, "waterworld");
    public static final RegistryObject<MenuType<SeawaterFilterMenu>> SEAWATER_FILTER =
            RegistryObject.create(ModBlocks.SEAWATER_FILTER.getId(), Registries.MENU, "waterworld");
    public static final RegistryObject<MenuType<OxygenMachineMenu>> ELECTRIC_AIR_COMPRESSOR =
            RegistryObject.create(ModBlocks.ELECTRIC_AIR_COMPRESSOR.getId(), Registries.MENU, "waterworld");
    public static final RegistryObject<MenuType<OxygenMachineMenu>> ELECTROLYTIC_OXYGEN_GENERATOR =
            RegistryObject.create(ModBlocks.ELECTROLYTIC_OXYGEN_GENERATOR.getId(), Registries.MENU, "waterworld");

    private ModMenus() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.MENU, ELECTRIC_AIR_COMPRESSOR.getId(), () -> new MenuType<>(
                (id, inventory) -> new OxygenMachineMenu(id, inventory, OxygenMachineType.COMPRESSOR), FeatureFlags.DEFAULT_FLAGS));
        event.register(Registries.MENU, ELECTROLYTIC_OXYGEN_GENERATOR.getId(), () -> new MenuType<>(
                (id, inventory) -> new OxygenMachineMenu(id, inventory, OxygenMachineType.ELECTROLYZER), FeatureFlags.DEFAULT_FLAGS));
        event.register(Registries.MENU, OXYGEN_TANKS.getId(), () -> IForgeMenuType.create(OxygenTankMenu::new));
        event.register(Registries.MENU, SEAWATER_FILTER.getId(),
                () -> new MenuType<>(SeawaterFilterMenu::new, FeatureFlags.DEFAULT_FLAGS));
    }
}
