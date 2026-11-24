package com.carrot123.waterworld.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

public class LayeredOceanBiomeSource extends BiomeSource {
    public static final Codec<LayeredOceanBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Biome.CODEC.fieldOf("ocean").forGetter(source -> source.ocean),
            Biome.CODEC.fieldOf("deep_ocean").forGetter(source -> source.deepOcean)
    ).apply(instance, LayeredOceanBiomeSource::new));

    private final Holder<Biome> ocean;
    private final Holder<Biome> deepOcean;

    public LayeredOceanBiomeSource(Holder<Biome> ocean, Holder<Biome> deepOcean) {
        this.ocean = ocean;
        this.deepOcean = deepOcean;
    }

    public static int ground(Climate.Sampler sampler, int blockX, int blockZ) {
        double region = sampler.continentalness().compute(new DensityFunction.SinglePointContext(blockX, 0, blockZ));
        return region < 0 ? OceanWorld.DEEP_GROUND : OceanWorld.SHALLOW_GROUND;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        return ground(sampler, QuartPos.toBlock(x), QuartPos.toBlock(z)) == OceanWorld.DEEP_GROUND ? deepOcean : ocean;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(ocean, deepOcean);
    }
}
