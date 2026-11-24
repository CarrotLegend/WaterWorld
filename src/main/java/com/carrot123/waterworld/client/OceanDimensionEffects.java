package com.carrot123.waterworld.client;

import com.carrot123.waterworld.worldgen.OceanWorld;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class OceanDimensionEffects {
    private OceanDimensionEffects() {
    }

    @SubscribeEvent
    public static void register(RegisterDimensionSpecialEffectsEvent event) {
        event.register(OceanWorld.LAYERED_NOISE.location(), new DimensionSpecialEffects.OverworldEffects() {
            @Override
            public float getCloudHeight() {
                return 928.0F;
            }
        });
    }
}
