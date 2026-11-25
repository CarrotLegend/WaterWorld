package com.carrot123.waterworld.registry;

import com.carrot123.waterworld.blockentity.ElectricEelGeneratorBlockEntity;
import com.carrot123.waterworld.blockentity.FishingBoxBlockEntity;
import com.carrot123.waterworld.blockentity.OxygenMachineBlockEntity;
import com.carrot123.waterworld.blockentity.RecyclingStationBlockEntity;
import com.carrot123.waterworld.blockentity.SeawaterFilterBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlockEntities {
    public static final RegistryObject<BlockEntityType<SeawaterFilterBlockEntity>> SEAWATER_FILTER =
            RegistryObject.create(ModBlocks.SEAWATER_FILTER.getId(), Registries.BLOCK_ENTITY_TYPE, "waterworld");
    public static final RegistryObject<BlockEntityType<ElectricEelGeneratorBlockEntity>> ELECTRIC_EEL_GENERATOR =
            RegistryObject.create(ModBlocks.ELECTRIC_EEL_GENERATOR.getId(), Registries.BLOCK_ENTITY_TYPE, "waterworld");
    public static final RegistryObject<BlockEntityType<FishingBoxBlockEntity>> FISHING_BOX =
            RegistryObject.create(ResourceLocation.fromNamespaceAndPath("waterworld", "fishing_box"),
                    Registries.BLOCK_ENTITY_TYPE, "waterworld");
    public static final RegistryObject<BlockEntityType<RecyclingStationBlockEntity>> RECYCLING_STATION =
            RegistryObject.create(ModBlocks.RECYCLING_STATION.getId(), Registries.BLOCK_ENTITY_TYPE, "waterworld");
    public static final RegistryObject<BlockEntityType<OxygenMachineBlockEntity>> ELECTRIC_AIR_COMPRESSOR =
            RegistryObject.create(ModBlocks.ELECTRIC_AIR_COMPRESSOR.getId(), Registries.BLOCK_ENTITY_TYPE, "waterworld");
    public static final RegistryObject<BlockEntityType<OxygenMachineBlockEntity>> ELECTROLYTIC_OXYGEN_GENERATOR =
            RegistryObject.create(ModBlocks.ELECTROLYTIC_OXYGEN_GENERATOR.getId(), Registries.BLOCK_ENTITY_TYPE, "waterworld");

    @SubscribeEvent
    public static void registerBlockEntities(RegisterEvent event) {
        event.register(Registries.BLOCK_ENTITY_TYPE, ELECTRIC_AIR_COMPRESSOR.getId(), () -> BlockEntityType.Builder.of(
                OxygenMachineBlockEntity::new, ModBlocks.ELECTRIC_AIR_COMPRESSOR.get()).build(null));
        event.register(Registries.BLOCK_ENTITY_TYPE, ELECTROLYTIC_OXYGEN_GENERATOR.getId(), () -> BlockEntityType.Builder.of(
                OxygenMachineBlockEntity::new, ModBlocks.ELECTROLYTIC_OXYGEN_GENERATOR.get()).build(null));
        event.register(Registries.BLOCK_ENTITY_TYPE, SEAWATER_FILTER.getId(),
                () -> BlockEntityType.Builder.of(SeawaterFilterBlockEntity::new, ModBlocks.SEAWATER_FILTER.get()).build(null));
        event.register(Registries.BLOCK_ENTITY_TYPE, ELECTRIC_EEL_GENERATOR.getId(),
                () -> BlockEntityType.Builder.of(ElectricEelGeneratorBlockEntity::new, ModBlocks.ELECTRIC_EEL_GENERATOR.get()).build(null));
        event.register(Registries.BLOCK_ENTITY_TYPE, FISHING_BOX.getId(),
                () -> BlockEntityType.Builder.of(FishingBoxBlockEntity::new,
                        ModBlocks.FISHING_BOX.get(), ModBlocks.ADVANCED_FISHING_BOX.get()).build(null));
        event.register(Registries.BLOCK_ENTITY_TYPE, RECYCLING_STATION.getId(),
                () -> BlockEntityType.Builder.of(RecyclingStationBlockEntity::new, ModBlocks.RECYCLING_STATION.get()).build(null));
    }
}
