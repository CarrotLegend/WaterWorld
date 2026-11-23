package com.carrot123.waterworld;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlockEntities {
    public static final RegistryObject<BlockEntityType<FishingBoxBlockEntity>> FISHING_BOX =
            RegistryObject.create(ResourceLocation.fromNamespaceAndPath("waterworld", "fishing_box"),
                    Registries.BLOCK_ENTITY_TYPE, "waterworld");
    public static final RegistryObject<BlockEntityType<RecyclingStationBlockEntity>> RECYCLING_STATION =
            RegistryObject.create(ModBlocks.RECYCLING_STATION.getId(), Registries.BLOCK_ENTITY_TYPE, "waterworld");

    @SubscribeEvent
    public static void registerBlockEntities(RegisterEvent event) {
        event.register(Registries.BLOCK_ENTITY_TYPE, FISHING_BOX.getId(),
                () -> BlockEntityType.Builder.of(FishingBoxBlockEntity::new,
                        ModBlocks.FISHING_BOX.get(), ModBlocks.ADVANCED_FISHING_BOX.get()).build(null));
        event.register(Registries.BLOCK_ENTITY_TYPE, RECYCLING_STATION.getId(),
                () -> BlockEntityType.Builder.of(RecyclingStationBlockEntity::new, ModBlocks.RECYCLING_STATION.get()).build(null));
    }
}
