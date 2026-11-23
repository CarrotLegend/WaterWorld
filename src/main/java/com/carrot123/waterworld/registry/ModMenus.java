package com.carrot123.waterworld.registry;

import com.carrot123.waterworld.menu.SeawaterFilterMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModMenus {
    public static final RegistryObject<MenuType<SeawaterFilterMenu>> SEAWATER_FILTER =
            RegistryObject.create(ModBlocks.SEAWATER_FILTER.getId(), Registries.MENU, "waterworld");

    private ModMenus() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.MENU, SEAWATER_FILTER.getId(),
                () -> new MenuType<>(SeawaterFilterMenu::new, FeatureFlags.DEFAULT_FLAGS));
    }
}
