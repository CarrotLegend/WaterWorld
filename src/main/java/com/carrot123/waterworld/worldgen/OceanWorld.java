package com.carrot123.waterworld.worldgen;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.LostCityProfileSetupImp;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public class OceanWorld {
    public static final String PROFILE = "waterworld_ocean";
    public static final String SHALLOW_PROFILE = "waterworld_layered_shallow";
    public static final String DEEP_PROFILE = "waterworld_layered_deep";
    public static final int GROUND = -42;
    public static final int LEGACY_SEA = 128;
    public static final int SEA = 800;
    public static final int MIN_BUILD_Y = -64;
    public static final int MAX_BUILD_Y = 1056;
    public static final int SHALLOW_GROUND = 500;
    public static final int DEEP_GROUND = 100;
    private static final ResourceLocation LAYERED_ID = ResourceLocation.fromNamespaceAndPath("waterworld", "layered_ocean");
    private static final ResourceKey<DimensionType> LAYERED_DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, LAYERED_ID);
    public static final ResourceKey<WorldPreset> PRESET = ResourceKey.create(Registries.WORLD_PRESET, LAYERED_ID);
    public static final ResourceKey<NoiseGeneratorSettings> NOISE = ResourceKey.create(Registries.NOISE_SETTINGS,
            ResourceLocation.fromNamespaceAndPath("waterworld", "ocean_world"));
    public static final ResourceKey<NoiseGeneratorSettings> LAYERED_NOISE = ResourceKey.create(Registries.NOISE_SETTINGS, LAYERED_ID);

    public static boolean isOcean(ChunkGenerator generator) {
        return generator instanceof NoiseBasedChunkGenerator noise
                && (noise.generatorSettings().is(NOISE) || noise.generatorSettings().is(LAYERED_NOISE));
    }

    public static boolean isOcean(Level level) {
        return level instanceof ServerLevel server && level.dimension().equals(Level.OVERWORLD)
                && isOcean(server.getChunkSource().getGenerator());
    }

    public static boolean isLayered(ChunkGenerator generator) {
        return generator instanceof NoiseBasedChunkGenerator noise && noise.generatorSettings().is(LAYERED_NOISE);
    }

    public static boolean isLayered(Level level) {
        return level instanceof ServerLevel server && level.dimension().equals(Level.OVERWORLD)
                && isLayered(server.getChunkSource().getGenerator());
    }

    public static Biome.Precipitation precipitationAt(Level level, Biome biome, BlockPos pos) {
        Biome.Precipitation precipitation = biome.getPrecipitationAt(pos);
        if (precipitation != Biome.Precipitation.SNOW) {
            return precipitation;
        }
        boolean layered = level.isClientSide
                ? level.dimension().equals(Level.OVERWORLD) && level.dimensionTypeId().equals(LAYERED_DIMENSION_TYPE)
                : isLayered(level);
        return layered ? Biome.Precipitation.RAIN : precipitation;
    }

    public static int sea(ChunkGenerator generator) {
        return isLayered(generator) ? SEA : LEGACY_SEA;
    }

    public static int sea(ServerLevel level) {
        return sea(level.getChunkSource().getGenerator());
    }

    public static int ground(ServerLevel level, int x, int z) {
        return LayeredOceanBiomeSource.ground(level.getChunkSource().randomState().sampler(), x, z);
    }

    public static WorldDimensions forceOverworld(RegistryAccess access, WorldDimensions dimensions) {
        WorldDimensions ocean = access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(PRESET).value().createWorldDimensions();
        LevelStem stem = ocean.dimensions().get(LevelStem.OVERWORLD);
        return new WorldDimensions(WorldDimensions.withOverworld(dimensions.dimensions(), stem.type(), stem.generator()));
    }

    public static void setupProfile() {
        setupProfile(PROFILE, GROUND, LEGACY_SEA, -64, LEGACY_SEA);
        setupProfile(SHALLOW_PROFILE, SHALLOW_GROUND, SEA, SHALLOW_GROUND - 1, SHALLOW_GROUND + 2);
        setupProfile(DEEP_PROFILE, DEEP_GROUND, SEA, DEEP_GROUND - 1, DEEP_GROUND + 2);
    }

    private static void setupProfile(String name, int ground, int sea, int minHeight, int maxHeight) {
        LostCityProfile profile = (LostCityProfile) new LostCityProfileSetupImp().createProfile(name, "onlycities");
        int shift = ground - profile.GROUNDLEVEL;
        profile.setGroundLevel(ground);
        profile.SEALEVEL = sea;
        profile.setCityLevelHeights(profile.CITY_LEVEL0_HEIGHT + shift, profile.CITY_LEVEL1_HEIGHT + shift,
                profile.CITY_LEVEL2_HEIGHT + shift, profile.CITY_LEVEL3_HEIGHT + shift,
                profile.CITY_LEVEL4_HEIGHT + shift, profile.CITY_LEVEL5_HEIGHT + shift,
                profile.CITY_LEVEL6_HEIGHT + shift, profile.CITY_LEVEL7_HEIGHT + shift);
        profile.CITY_MINHEIGHT = minHeight;
        profile.CITY_MAXHEIGHT = maxHeight;
        if (!PROFILE.equals(name)) {
            profile.RAILWAYS_CAN_END = true;
        }
        profile.EXPLOSION_MINHEIGHT += shift;
        profile.EXPLOSION_MAXHEIGHT += shift;
        profile.MINI_EXPLOSION_MINHEIGHT += shift;
        profile.MINI_EXPLOSION_MAXHEIGHT += shift;
        profile.setDescription("海洋世界：原生 onlycities 海底城市");
    }
}
