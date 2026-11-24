package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.worldgen.LayeredCityInfo;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.worldgen.DefaultDimensionInfo;
import mcjty.lostcities.worldgen.lost.cityassets.WorldStyle;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DefaultDimensionInfo.class, remap = false)
public class DefaultDimensionInfoMixin {
    @Redirect(method = "<init>(Lnet/minecraft/world/level/WorldGenLevel;Lmcjty/lostcities/config/LostCityProfile;Lmcjty/lostcities/config/LostCityProfile;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;", remap = true),
            require = 6, allow = 6)
    private ResourceKey<Level> waterworld$cityCacheKey(ServerLevel level, WorldGenLevel world,
                                                      LostCityProfile profile, LostCityProfile outside) {
        return LayeredCityInfo.planningKey(profile, level);
    }

    @Inject(method = "applyHighwayCityConstraints(IIF)F", at = @At("HEAD"), cancellable = true)
    private void waterworld$restrictCityHub(int x, int z, float potential, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof LayeredCityInfo info && !info.allows(x, z)) {
            cir.setReturnValue(0.0F);
        }
    }

    @Redirect(method = "applyHighwayCityConstraints(IIF)F",
            at = @At(value = "INVOKE", target = "Lmcjty/lostcities/worldgen/lost/cityassets/WorldStyle;getCityChanceMultiplier(Lnet/minecraft/core/Holder;)F"),
            require = 1, allow = 1)
    private float waterworld$oceanHighwayChance(WorldStyle style, Holder<Biome> biome) {
        return (Object) this instanceof LayeredCityInfo ? 1.0F : style.getCityChanceMultiplier(biome);
    }
}
