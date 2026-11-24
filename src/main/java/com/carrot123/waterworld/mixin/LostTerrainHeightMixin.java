package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.worldgen.LayeredCityInfo;
import com.carrot123.waterworld.worldgen.OceanWorld;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.ChunkHeightmap;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import net.minecraft.world.level.WorldGenLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LostCityTerrainFeature.class, remap = false)
public class LostTerrainHeightMixin {
    @Shadow @Final public IDimensionInfo provider;

    @Inject(method = "getHeightmap(Lmcjty/lostcities/varia/ChunkCoord;Lnet/minecraft/world/level/WorldGenLevel;)Lmcjty/lostcities/worldgen/ChunkHeightmap;",
            at = @At("HEAD"), cancellable = true)
    private void waterworld$terrainHeight(ChunkCoord coord, WorldGenLevel world, CallbackInfoReturnable<ChunkHeightmap> cir) {
        if (provider instanceof LayeredCityInfo info) {
            cir.setReturnValue(info.terrainHeight(coord));
        }
    }

    @ModifyConstant(method = "breakBlocksForDamageNew(IILmcjty/lostcities/worldgen/lost/BuildingInfo;)V",
            slice = @Slice(to = @At(value = "INVOKE", target = "Lmcjty/lostcities/worldgen/lost/DamageArea;hasExplosions(I)Z")),
            constant = @Constant(intValue = 0, ordinal = 1), require = 1, allow = 1)
    private int waterworld$firstDamageSection(int section) {
        return provider instanceof LayeredCityInfo ? OceanWorld.MIN_BUILD_Y >> 4 : section;
    }

    @ModifyConstant(method = "breakBlocksForDamageNew(IILmcjty/lostcities/worldgen/lost/BuildingInfo;)V",
            slice = @Slice(to = @At(value = "INVOKE", target = "Lmcjty/lostcities/worldgen/lost/DamageArea;hasExplosions(I)Z")),
            constant = @Constant(intValue = 16, ordinal = 2), require = 1, allow = 1)
    private int waterworld$lastDamageSection(int section) {
        return provider instanceof LayeredCityInfo ? OceanWorld.MAX_BUILD_Y >> 4 : section;
    }
}
