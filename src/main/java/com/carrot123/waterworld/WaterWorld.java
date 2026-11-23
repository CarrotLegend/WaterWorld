package com.carrot123.waterworld;

import com.carrot123.waterworld.worldgen.OceanSpawnPlatform;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;

@Mod("waterworld")
public class WaterWorld {
    public WaterWorld() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, OceanSpawnPlatform::create);
    }
}
