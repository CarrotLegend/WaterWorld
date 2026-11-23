package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.OceanWorld;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.worldgen.LostCityFeature;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LostCityFeature.class, remap = false)
public class LostCityFeatureMixin {
    @Redirect(method = "getOrCreateDimensionInfo(Lnet/minecraft/world/level/WorldGenLevel;)Lmcjty/lostcities/worldgen/IDimensionInfo;",
            at = @At(value = "INVOKE", target = "Lmcjty/lostcities/setup/Config;getProfileForDimension(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/String;"))
    private String waterworld$profile(ResourceKey<Level> dimension, WorldGenLevel world) {
        if (OceanWorld.isOcean(world.getLevel())) {
            return OceanWorld.PROFILE;
        }
        return Config.getProfileForDimension(dimension);
    }
}
