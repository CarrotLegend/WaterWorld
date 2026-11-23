package com.carrot123.waterworld;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
    public static final RegistryObject<Item> SCRAP_IRON = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "scrap_iron"), Registries.ITEM, "waterworld");
    public static final RegistryObject<Item> SCREW = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "screw"), Registries.ITEM, "waterworld");
    public static final RegistryObject<Item> RECYCLING_HAMMER = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "recycling_hammer"), Registries.ITEM, "waterworld");

    @SubscribeEvent
    public static void registerItems(RegisterEvent event) {
        event.register(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("waterworld", "fishing_box"),
                () -> new BlockItem(ModBlocks.FISHING_BOX.get(), new Item.Properties()));
        event.register(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("waterworld", "advanced_fishing_box"),
                () -> new BlockItem(ModBlocks.ADVANCED_FISHING_BOX.get(), new Item.Properties()));
        event.register(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("waterworld", "electric_eel"),
                () -> new Item(new Item.Properties().stacksTo(64).food(Foods.TROPICAL_FISH)));
        event.register(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("waterworld", "plastic"),
                () -> new Item(new Item.Properties().stacksTo(64)));
        event.register(Registries.ITEM, SCREW.getId(),
                () -> new Item(new Item.Properties().stacksTo(64)));
        event.register(Registries.ITEM, SCRAP_IRON.getId(),
                () -> new Item(new Item.Properties().stacksTo(64)));
        event.register(Registries.ITEM, RECYCLING_HAMMER.getId(),
                () -> new Item(new Item.Properties().stacksTo(1)));
        event.register(Registries.ITEM, ModBlocks.RECYCLING_STATION.getId(),
                () -> new BlockItem(ModBlocks.RECYCLING_STATION.get(), new Item.Properties()));
    }
}
