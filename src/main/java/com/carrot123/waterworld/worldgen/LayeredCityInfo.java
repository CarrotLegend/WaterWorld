package com.carrot123.waterworld.worldgen;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.ArrayList;
import java.util.List;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.ChunkHeightmap;
import mcjty.lostcities.worldgen.DefaultDimensionInfo;
import mcjty.lostcities.worldgen.highway.HighwayInfo;
import mcjty.lostcities.worldgen.highway.HighwayRoute;
import mcjty.lostcities.worldgen.highway.HighwaySegment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public final class LayeredCityInfo extends DefaultDimensionInfo {
    private static final int BUFFER_CHUNKS = 2;
    private static final int SHALLOW = 1;
    private static final int DEEP = 2;
    private static final ResourceKey<Level> SHALLOW_KEY = key("layered_shallow_cities");
    private static final ResourceKey<Level> DEEP_KEY = key("layered_deep_cities");

    private final ServerLevel level;
    private final Cache<Long, Integer> masks = CacheBuilder.newBuilder().maximumSize(16384).build();
    private final Cache<Long, Boolean> eligibleChunks = CacheBuilder.newBuilder().maximumSize(8192).build();
    private final Cache<HighwayRoute, Boolean> routes = CacheBuilder.newBuilder().maximumSize(1024).build();

    public LayeredCityInfo(ServerLevel level, LostCityProfile profile) {
        super(level, profile, null);
        this.level = level;
    }

    private static ResourceKey<Level> key(String name) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("waterworld", name));
    }

    public static ResourceKey<Level> planningKey(LostCityProfile profile, ServerLevel level) {
        if (OceanWorld.isLayered(level)) {
            if (profile.getName().equals(OceanWorld.SHALLOW_PROFILE)) {
                return SHALLOW_KEY;
            }
            if (profile.getName().equals(OceanWorld.DEEP_PROFILE)) {
                return DEEP_KEY;
            }
        }
        return level.dimension();
    }

    public static String profileFor(ServerLevel level, ChunkPos pos) {
        return OceanWorld.ground(level, pos.getMiddleBlockX(), pos.getMiddleBlockZ()) == OceanWorld.SHALLOW_GROUND
                ? OceanWorld.SHALLOW_PROFILE : OceanWorld.DEEP_PROFILE;
    }

    public boolean allows(ChunkCoord coord) {
        return allows(coord.chunkX(), coord.chunkZ());
    }

    public boolean allows(int x, int z) {
        return eligibleChunks.asMap().computeIfAbsent(ChunkPos.asLong(x, z), ignored -> {
            for (int dx = -BUFFER_CHUNKS; dx <= BUFFER_CHUNKS; dx++) {
                for (int dz = -BUFFER_CHUNKS; dz <= BUFFER_CHUNKS; dz++) {
                    if (!samePlateau(x + dx, z + dz)) {
                        return false;
                    }
                }
            }
            return true;
        });
    }

    public boolean samePlateau(int x, int z) {
        return mask(x, z) == (getType().equals(SHALLOW_KEY) ? SHALLOW : DEEP);
    }

    private int mask(int x, int z) {
        return masks.asMap().computeIfAbsent(ChunkPos.asLong(x, z), ignored -> {
            int mask = 0;
            for (int dx = 0; dx < 16; dx += 4) {
                for (int dz = 0; dz < 16; dz += 4) {
                    mask |= OceanWorld.ground(level, (x << 4) + dx, (z << 4) + dz) == OceanWorld.SHALLOW_GROUND
                            ? SHALLOW : DEEP;
                }
                if (mask == (SHALLOW | DEEP)) {
                    break;
                }
            }
            return mask;
        });
    }

    public boolean allowsSpan(ChunkCoord start, ChunkCoord end) {
        return allowsSpan(start.chunkX(), start.chunkZ(), end.chunkX(), end.chunkZ());
    }

    private boolean allowsSpan(int startX, int startZ, int endX, int endZ) {
        if (startX != endX && startZ != endZ) {
            return false;
        }
        int dx = Integer.compare(endX, startX);
        int dz = Integer.compare(endZ, startZ);
        int x = startX;
        int z = startZ;
        while (allows(x, z)) {
            if (x == endX && z == endZ) {
                return true;
            }
            x += dx;
            z += dz;
        }
        return false;
    }

    private boolean allowsRoute(HighwayRoute route) {
        return routes.asMap().computeIfAbsent(route, candidate -> {
            for (HighwaySegment segment : candidate.segments()) {
                if (!allowsSpan(segment.startX(), segment.startZ(), segment.endX(), segment.endZ())) {
                    return false;
                }
            }
            return true;
        });
    }

    public HighwayInfo filterHighways(HighwayInfo info) {
        if (info.routeHits().isEmpty()) {
            return info;
        }
        List<HighwayInfo.RouteHit> hits = new ArrayList<>();
        int xLevel = -1;
        int zLevel = -1;
        for (HighwayInfo.RouteHit hit : info.routeHits()) {
            if (allowsRoute(hit.route())) {
                hits.add(hit);
                int level = hit.route().highwayLevel();
                if (hit.xAxis()) {
                    xLevel = xLevel < 0 ? level : Math.min(xLevel, level);
                }
                if (hit.zAxis()) {
                    zLevel = zLevel < 0 ? level : Math.min(zLevel, level);
                }
            }
        }
        if (hits.size() == info.routeHits().size()) {
            return info;
        }
        if (xLevel < 0 && zLevel < 0) {
            return HighwayInfo.NONE;
        }
        HighwayInfo.Classification type;
        if (xLevel >= 0 && zLevel >= 0) {
            type = xLevel == zLevel ? HighwayInfo.Classification.SAME_LEVEL_INTERSECTION
                    : HighwayInfo.Classification.MULTI_LEVEL_INTERSECTION;
        } else {
            type = xLevel >= 0 ? HighwayInfo.Classification.X_HIGHWAY : HighwayInfo.Classification.Z_HIGHWAY;
        }
        return new HighwayInfo(xLevel, zLevel, type, hits);
    }

    public ChunkHeightmap terrainHeight(ChunkCoord coord) {
        int x = (coord.chunkX() << 4) + 8;
        int z = (coord.chunkZ() << 4) + 8;
        int top = OceanWorld.ground(level, x, z);
        ChunkHeightmap height = new ChunkHeightmap(getProfile().LANDSCAPE_TYPE, top);
        height.setHeight(top + 1);
        return height;
    }

    @Override
    public Holder<Biome> getBiome(BlockPos pos) {
        var chunks = level.getChunkSource();
        return chunks.getGenerator().getBiomeSource().getNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2,
                chunks.randomState().sampler());
    }
}
