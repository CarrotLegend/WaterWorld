package com.carrot123.waterworld.registry;

import com.carrot123.waterworld.block.ElectricEelGeneratorBlock;
import com.carrot123.waterworld.block.FishingBoxBlock;
import com.carrot123.waterworld.block.OxygenMachineBlock;
import com.carrot123.waterworld.block.RecyclingStationBlock;
import com.carrot123.waterworld.block.SeawaterFilterBlock;
import com.carrot123.waterworld.oxygen.OxygenMachineType;
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
    public static final RegistryObject<Block> SEAWATER_FILTER = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "seawater_filter"), Registries.BLOCK, "waterworld");
    public static final RegistryObject<Block> ELECTRIC_EEL_GENERATOR = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "electric_eel_generator"), Registries.BLOCK, "waterworld");
    public static final RegistryObject<Block> RECYCLING_STATION = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "recycling_station"), Registries.BLOCK, "waterworld");
    public static final RegistryObject<Block> FISHING_BOX = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "fishing_box"), Registries.BLOCK, "waterworld");
    public static final RegistryObject<Block> ADVANCED_FISHING_BOX = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "advanced_fishing_box"), Registries.BLOCK, "waterworld");
    public static final RegistryObject<OxygenMachineBlock> ELECTRIC_AIR_COMPRESSOR = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", OxygenMachineType.COMPRESSOR.id()), Registries.BLOCK, "waterworld");
    public static final RegistryObject<OxygenMachineBlock> ELECTROLYTIC_OXYGEN_GENERATOR = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", OxygenMachineType.ELECTROLYZER.id()), Registries.BLOCK, "waterworld");

    @SubscribeEvent
    public static void registerBlocks(RegisterEvent event) {
        event.register(Registries.BLOCK, ELECTRIC_AIR_COMPRESSOR.getId(),
                () -> new OxygenMachineBlock(OxygenMachineType.COMPRESSOR,
                        BlockBehaviour.Properties.of().strength(4.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
        event.register(Registries.BLOCK, ELECTROLYTIC_OXYGEN_GENERATOR.getId(),
                () -> new OxygenMachineBlock(OxygenMachineType.ELECTROLYZER,
                        BlockBehaviour.Properties.of().strength(4.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
        event.register(Registries.BLOCK, SEAWATER_FILTER.getId(), () -> new SeawaterFilterBlock(
                BlockBehaviour.Properties.of().strength(3.0F, 5.0F).sound(SoundType.WOOD)));
        event.register(Registries.BLOCK, ELECTRIC_EEL_GENERATOR.getId(), () -> new ElectricEelGeneratorBlock(
                BlockBehaviour.Properties.of().strength(4.0F, 6.0F).sound(SoundType.METAL).noOcclusion()));
        event.register(Registries.BLOCK, RECYCLING_STATION.getId(), () -> new RecyclingStationBlock(
                BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.WOOD).noOcclusion()));
        event.register(Registries.BLOCK, FISHING_BOX.getId(), () -> new FishingBoxBlock(false,
                BlockBehaviour.Properties.copy(Blocks.BARREL).strength(2.5F)));
        event.register(Registries.BLOCK, ADVANCED_FISHING_BOX.getId(), () -> new FishingBoxBlock(true,
                BlockBehaviour.Properties.copy(Blocks.BARREL).strength(4.0F)));
    }
}
