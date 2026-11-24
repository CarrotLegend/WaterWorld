package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.worldgen.OceanWorld;
import com.carrot123.waterworld.worldgen.LayeredCityInfo;
import java.util.Map;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.ProfileSetup;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.LostCityFeature;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LostCityFeature.class, remap = false)
public class LostCityFeatureMixin {
    @Shadow @Final private Map<ResourceKey<Level>, IDimensionInfo> dimensionInfo;

    @Inject(method = "getOrCreateDimensionInfo(Lnet/minecraft/world/level/WorldGenLevel;)Lmcjty/lostcities/worldgen/IDimensionInfo;",
            at = @At("HEAD"), cancellable = true)
    private void waterworld$layeredProfile(WorldGenLevel world, CallbackInfoReturnable<IDimensionInfo> cir) {
        if (OceanWorld.isLayered(world.getLevel())) {
            ServerLevel level = world.getLevel();
            ChunkPos pos = world instanceof WorldGenRegion region ? region.getCenter() : new ChunkPos(level.getSharedSpawnPos());
            LostCityProfile profile = ProfileSetup.STANDARD_PROFILES.get(LayeredCityInfo.profileFor(level, pos));
            ResourceKey<Level> key = LayeredCityInfo.planningKey(profile, level);
            LayeredCityInfo info = (LayeredCityInfo) dimensionInfo.computeIfAbsent(key, ignored -> new LayeredCityInfo(level, profile));
            cir.setReturnValue(world instanceof WorldGenRegion && !info.allows(pos.x, pos.z) ? null : info);
        }
    }

    @Redirect(method = "getOrCreateDimensionInfo(Lnet/minecraft/world/level/WorldGenLevel;)Lmcjty/lostcities/worldgen/IDimensionInfo;",
            at = @At(value = "INVOKE", target = "Lmcjty/lostcities/setup/Config;getProfileForDimension(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/String;"))
    private String waterworld$profile(ResourceKey<Level> dimension, WorldGenLevel world) {
        if (OceanWorld.isOcean(world.getLevel())) {
            return OceanWorld.PROFILE;
        }
        return Config.getProfileForDimension(dimension);
    }
}
