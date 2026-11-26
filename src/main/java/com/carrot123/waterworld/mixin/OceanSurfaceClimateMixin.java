package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.worldgen.OceanWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public class OceanSurfaceClimateMixin {
    @Inject(method = "shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Z)Z",
            at = @At("HEAD"), cancellable = true, require = 1, allow = 1)
    private void waterworld$protectSeaSurface(LevelReader reader, BlockPos pos, boolean checkNeighbors,
                                              CallbackInfoReturnable<Boolean> cir) {
        if (pos.getY() == OceanWorld.SEA && waterworld$isLayered(reader)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldSnow(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("HEAD"), cancellable = true, require = 1, allow = 1)
    private void waterworld$protectSeaSurfaceFromSnow(LevelReader reader, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (reader instanceof ServerLevel level && OceanWorld.isLayered(level)) {
            cir.setReturnValue(false);
            return;
        }
        if (pos.getY() == OceanWorld.SEA + 1 && waterworld$isLayered(reader)) {
            BlockState below = reader.getBlockState(pos.below());
            if (below.getFluidState().is(FluidTags.WATER) || below.is(Blocks.ICE) || below.is(Blocks.FROSTED_ICE)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Unique
    private static boolean waterworld$isLayered(LevelReader reader) {
        if (reader instanceof WorldGenRegion region) {
            return OceanWorld.isLayered(region.getLevel());
        }
        return reader instanceof ServerLevel level && OceanWorld.isLayered(level);
    }
}
