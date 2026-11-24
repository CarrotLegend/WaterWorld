package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.worldgen.LayeredCityInfo;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.Railway;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Railway.class, remap = false)
public class LostRailwayBoundsMixin {
    @Inject(method = "getRailChunkType(Lmcjty/lostcities/varia/ChunkCoord;Lmcjty/lostcities/worldgen/IDimensionInfo;Lmcjty/lostcities/config/LostCityProfile;)Lmcjty/lostcities/worldgen/lost/Railway$RailChunkInfo;",
            at = @At("HEAD"), cancellable = true)
    private static void waterworld$restrictRail(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile,
                                                CallbackInfoReturnable<Railway.RailChunkInfo> cir) {
        if (provider instanceof LayeredCityInfo info && !info.allows(coord)) {
            cir.setReturnValue(Railway.RailChunkInfo.NOTHING);
        }
    }
}
