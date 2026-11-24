package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.worldgen.LayeredCityInfo;
import com.carrot123.waterworld.worldgen.OceanWorld;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BuildingInfo.class, remap = false)
public class LostBuildingBoundsMixin {
    @Shadow @Final public IDimensionInfo provider;
    @Shadow @Final public ChunkCoord coord;

    @Inject(method = "isCityRaw(Lmcjty/lostcities/varia/ChunkCoord;Lmcjty/lostcities/worldgen/IDimensionInfo;Lmcjty/lostcities/config/LostCityProfile;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void waterworld$restrictCities(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile,
                                                  CallbackInfoReturnable<Boolean> cir) {
        if (provider instanceof LayeredCityInfo info && !info.allows(coord)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isSuitableForBridge(Lmcjty/lostcities/worldgen/IDimensionInfo;Lmcjty/lostcities/worldgen/lost/BuildingInfo;)Z",
            at = @At("HEAD"), cancellable = true)
    private void waterworld$restrictBridge(IDimensionInfo provider, BuildingInfo candidate, CallbackInfoReturnable<Boolean> cir) {
        if (provider instanceof LayeredCityInfo info && !info.allows(candidate.coord)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canRailGoThrough()Z", at = @At("HEAD"), cancellable = true)
    private void waterworld$restrictCorridor(CallbackInfoReturnable<Boolean> cir) {
        if (provider instanceof LayeredCityInfo info && !info.allows(coord)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "hasRoadConnection(Lmcjty/lostcities/worldgen/lost/BuildingInfo;Lmcjty/lostcities/worldgen/lost/BuildingInfo;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void waterworld$restrictStreet(BuildingInfo first, BuildingInfo second, CallbackInfoReturnable<Boolean> cir) {
        if (first.provider instanceof LayeredCityInfo info
                && (second.provider != first.provider || !info.allows(first.coord) || !info.allows(second.coord))) {
            cir.setReturnValue(false);
        }
    }

    @ModifyConstant(method = "getDesiredMaxHeightL1Locked()Lmcjty/lostcities/worldgen/lost/BuildingInfo$MinMax;",
            constant = @Constant(intValue = 256), require = 1, allow = 1)
    private int waterworld$terrainHeightL1(int height) {
        return provider instanceof LayeredCityInfo ? OceanWorld.MAX_BUILD_Y : height;
    }

    @ModifyConstant(method = "getDesiredMaxHeightL2Locked()Lmcjty/lostcities/worldgen/lost/BuildingInfo$MinMax;",
            constant = @Constant(intValue = 256), require = 1, allow = 1)
    private int waterworld$terrainHeightL2(int height) {
        return provider instanceof LayeredCityInfo ? OceanWorld.MAX_BUILD_Y : height;
    }
}
