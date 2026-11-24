package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.worldgen.LayeredCityInfo;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.highway.HighwayInfo;
import mcjty.lostcities.worldgen.lost.Highway;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Highway.class, remap = false)
public class LostHighwayBoundsMixin {
    @Inject(method = "getHighwayInfo(Lmcjty/lostcities/varia/ChunkCoord;Lmcjty/lostcities/worldgen/IDimensionInfo;Lmcjty/lostcities/config/LostCityProfile;)Lmcjty/lostcities/worldgen/highway/HighwayInfo;",
            at = @At("HEAD"), cancellable = true)
    private static void waterworld$restrictHighway(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile,
                                                   CallbackInfoReturnable<HighwayInfo> cir) {
        if (provider instanceof LayeredCityInfo info && !info.allows(coord)) {
            cir.setReturnValue(HighwayInfo.NONE);
        }
    }

    @Inject(method = "getHighwayInfo(Lmcjty/lostcities/varia/ChunkCoord;Lmcjty/lostcities/worldgen/IDimensionInfo;Lmcjty/lostcities/config/LostCityProfile;)Lmcjty/lostcities/worldgen/highway/HighwayInfo;",
            at = @At("RETURN"), cancellable = true)
    private static void waterworld$restrictHighwayRoute(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile,
                                                        CallbackInfoReturnable<HighwayInfo> cir) {
        if (provider instanceof LayeredCityInfo info) {
            cir.setReturnValue(info.filterHighways(cir.getReturnValue()));
        }
    }
}
