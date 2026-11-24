package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.worldgen.LayeredCityInfo;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.City;
import mcjty.lostcities.worldgen.lost.cityassets.WorldStyle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = City.class, remap = false)
public class LostCityRegionMixin {
    @Inject(method = "isCityCenter(Lmcjty/lostcities/varia/ChunkCoord;Lmcjty/lostcities/worldgen/IDimensionInfo;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void waterworld$makeCentersLocal(ChunkCoord coord, IDimensionInfo provider, CallbackInfoReturnable<Boolean> cir) {
        if (provider instanceof LayeredCityInfo info && !info.samePlateau(coord.chunkX(), coord.chunkZ())) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "getCityFactor(Lmcjty/lostcities/varia/ChunkCoord;Lmcjty/lostcities/worldgen/IDimensionInfo;Lmcjty/lostcities/config/LostCityProfile;)F",
            at = @At(value = "INVOKE", target = "Lmcjty/lostcities/worldgen/lost/cityassets/WorldStyle;getCityChanceMultiplier(Lmcjty/lostcities/worldgen/IDimensionInfo;Lmcjty/lostcities/varia/ChunkCoord;)F"),
            require = 1, allow = 1)
    private static float waterworld$oceanCityChance(WorldStyle style, IDimensionInfo provider, ChunkCoord coord) {
        return provider instanceof LayeredCityInfo ? 1.0F : style.getCityChanceMultiplier(provider, coord);
    }
}
