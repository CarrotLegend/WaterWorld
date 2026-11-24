package com.carrot123.waterworld.pressure;

import com.carrot123.waterworld.worldgen.OceanWorld;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;

public final class WaterPressure {
    public static final int BASE_RESISTANCE = 4;
    public static final ResourceKey<DamageType> DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("waterworld", "water_pressure"));

    private WaterPressure() {
    }

    public static double depth(ServerLevel level, double y) {
        return depthBelow(OceanWorld.sea(level), y);
    }

    public static double environmentalPressure(ServerLevel level, Player player) {
        if (!player.isInWater()) {
            return pressure(0.0D);
        }
        double depth = OceanWorld.isOcean(level)
                ? depth(level, player.getY()) : depthBelow(level.getSeaLevel(), player.getY());
        return pressure(depth);
    }

    private static double depthBelow(double sea, double y) {
        return Math.max(0.0D, sea - y);
    }

    public static double pressure(double depth) {
        return 1.0D + depth / 10.0D;
    }

    public static int resistance(Player player) {
        return resistance(DivingSuitType.wornBy(player));
    }

    public static int resistance(@Nullable DivingSuitType suit) {
        return suit == null ? BASE_RESISTANCE : suit.resistance();
    }

    public static int maxDepth(int resistance) {
        return (resistance - 1) * 10;
    }

    static DamageSource damageSource(ServerLevel level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DAMAGE_TYPE));
    }
}
