package com.carrot123.waterworld;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.LostCityProfileSetupImp;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

public class OceanWorld {
    public static final String PROFILE = "waterworld_ocean";
    public static final int GROUND = -42;
    public static final int SEA = 128;
    public static final ResourceKey<WorldPreset> PRESET = ResourceKey.create(Registries.WORLD_PRESET, new ResourceLocation("waterworld", "ocean_world"));
    public static final ResourceKey<NoiseGeneratorSettings> NOISE = ResourceKey.create(Registries.NOISE_SETTINGS, new ResourceLocation("waterworld", "ocean_world"));

    public static boolean isOcean(ChunkGenerator generator) {
        return generator instanceof NoiseBasedChunkGenerator noise && noise.generatorSettings().is(NOISE);
    }

    public static boolean isOcean(Level level) {
        return level instanceof ServerLevel server && level.dimension().equals(Level.OVERWORLD)
                && isOcean(server.getChunkSource().getGenerator());
    }

    public static WorldDimensions forceOverworld(RegistryAccess access, WorldDimensions dimensions) {
        WorldDimensions ocean = access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(PRESET).value().createWorldDimensions();
        return dimensions.replaceOverworldGenerator(access, ocean.overworld());
    }

    public static void setupProfile() {
        LostCityProfile profile = (LostCityProfile) new LostCityProfileSetupImp().createProfile(PROFILE, "onlycities");
        int shift = GROUND - profile.GROUNDLEVEL;
        profile.setGroundLevel(GROUND);
        profile.SEALEVEL = SEA;
        profile.setCityLevelHeights(profile.CITY_LEVEL0_HEIGHT + shift, profile.CITY_LEVEL1_HEIGHT + shift,
                profile.CITY_LEVEL2_HEIGHT + shift, profile.CITY_LEVEL3_HEIGHT + shift,
                profile.CITY_LEVEL4_HEIGHT + shift, profile.CITY_LEVEL5_HEIGHT + shift,
                profile.CITY_LEVEL6_HEIGHT + shift, profile.CITY_LEVEL7_HEIGHT + shift);
        profile.CITY_MINHEIGHT = -64;
        profile.CITY_MAXHEIGHT = SEA;
        profile.EXPLOSION_MINHEIGHT += shift;
        profile.EXPLOSION_MAXHEIGHT += shift;
        profile.MINI_EXPLOSION_MINHEIGHT += shift;
        profile.MINI_EXPLOSION_MAXHEIGHT += shift;
        profile.setDescription("海洋世界：原生 onlycities 海底城市");
    }
}
