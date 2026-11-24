package com.carrot123.waterworld.worldgen;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record OceanRegionDensity(NoiseHolder noise) implements DensityFunction.SimpleFunction {
    public static final KeyDispatchDataCodec<OceanRegionDensity> CODEC =
            KeyDispatchDataCodec.of(NoiseHolder.CODEC.fieldOf("noise").xmap(OceanRegionDensity::new, OceanRegionDensity::noise).codec());

    @Override
    public double compute(FunctionContext context) {
        int x = context.blockX() & ~3;
        int z = context.blockZ() & ~3;
        return noise.getValue(x / 32.0, 0, z / 32.0);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new OceanRegionDensity(visitor.visitNoise(noise)));
    }

    @Override
    public double minValue() {
        return -noise.maxValue();
    }

    @Override
    public double maxValue() {
        return noise.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
