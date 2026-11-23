package com.carrot123.waterworld;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlocks {
    public static final RegistryObject<Block> RECYCLING_STATION = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "recycling_station"), Registries.BLOCK, "waterworld");
    public static final RegistryObject<Block> FISHING_BOX = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "fishing_box"), Registries.BLOCK, "waterworld");
    public static final RegistryObject<Block> ADVANCED_FISHING_BOX = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "advanced_fishing_box"), Registries.BLOCK, "waterworld");

    @SubscribeEvent
    public static void registerBlocks(RegisterEvent event) {
        event.register(Registries.BLOCK, RECYCLING_STATION.getId(), () -> new RecyclingStationBlock(
                BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.WOOD).noOcclusion()));
        event.register(Registries.BLOCK, FISHING_BOX.getId(), () -> new FishingBoxBlock(false,
                BlockBehaviour.Properties.copy(Blocks.BARREL).strength(2.5F)));
        event.register(Registries.BLOCK, ADVANCED_FISHING_BOX.getId(), () -> new FishingBoxBlock(true,
                BlockBehaviour.Properties.copy(Blocks.BARREL).strength(4.0F)));
    }
}
