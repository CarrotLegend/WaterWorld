package com.carrot123.waterworld.registry;

import com.carrot123.waterworld.entity.GeneratorEelEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntities {
    public static final RegistryObject<EntityType<GeneratorEelEntity>> GENERATOR_EEL = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "generator_eel"), Registries.ENTITY_TYPE, "waterworld");

    private ModEntities() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.ENTITY_TYPE, GENERATOR_EEL.getId(),
                () -> EntityType.Builder.<GeneratorEelEntity>of(GeneratorEelEntity::new, MobCategory.MISC)
                        .sized(0.25F, 1.1F).noSummon().fireImmune().clientTrackingRange(8).updateInterval(1)
                        .build(GENERATOR_EEL.getId().toString()));
    }
}
