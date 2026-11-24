package com.carrot123.waterworld.registry;

import com.carrot123.waterworld.worldgen.LayeredOceanBiomeSource;
import com.carrot123.waterworld.worldgen.OceanRegionDensity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModWorldgen {
    private ModWorldgen() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.BIOME_SOURCE, ResourceLocation.fromNamespaceAndPath("waterworld", "layered_ocean"),
                () -> LayeredOceanBiomeSource.CODEC);
        event.register(Registries.DENSITY_FUNCTION_TYPE, ResourceLocation.fromNamespaceAndPath("waterworld", "ocean_region"),
                () -> OceanRegionDensity.CODEC.codec());
    }
}
