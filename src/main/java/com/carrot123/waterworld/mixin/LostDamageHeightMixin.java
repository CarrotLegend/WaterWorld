package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.worldgen.LayeredCityInfo;
import com.carrot123.waterworld.worldgen.OceanWorld;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.DamageArea;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DamageArea.class, remap = false)
public abstract class LostDamageHeightMixin {
    @Shadow public abstract boolean hasExplosions(int section);
    @Unique private boolean waterworld$layered;

    @Inject(method = "<init>(IILmcjty/lostcities/worldgen/IDimensionInfo;Lmcjty/lostcities/worldgen/lost/BuildingInfo;)V",
            at = @At("RETURN"))
    private void waterworld$recordLayer(int x, int z, IDimensionInfo provider, BuildingInfo info, CallbackInfo ci) {
        waterworld$layered = provider instanceof LayeredCityInfo;
    }

    @ModifyConstant(method = "<init>(IILmcjty/lostcities/worldgen/IDimensionInfo;Lmcjty/lostcities/worldgen/lost/BuildingInfo;)V",
            constant = @Constant(doubleValue = 0.0), require = 1, allow = 1)
    private double waterworld$damageMin(double y, int x, int z, IDimensionInfo provider, BuildingInfo info) {
        return provider instanceof LayeredCityInfo ? OceanWorld.MIN_BUILD_Y : y;
    }

    @ModifyConstant(method = "<init>(IILmcjty/lostcities/worldgen/IDimensionInfo;Lmcjty/lostcities/worldgen/lost/BuildingInfo;)V",
            constant = @Constant(doubleValue = 256.0), require = 1, allow = 1)
    private double waterworld$damageMax(double y, int x, int z, IDimensionInfo provider, BuildingInfo info) {
        return provider instanceof LayeredCityInfo ? OceanWorld.MAX_BUILD_Y : y;
    }

    @Inject(method = "getLowestExplosionHeight()I", at = @At("HEAD"), cancellable = true)
    private void waterworld$lowestExplosion(CallbackInfoReturnable<Integer> cir) {
        if (waterworld$layered) {
            for (int section = OceanWorld.MIN_BUILD_Y >> 4; section < (OceanWorld.MAX_BUILD_Y >> 4); section++) {
                if (hasExplosions(section)) {
                    cir.setReturnValue(section << 4);
                    return;
                }
            }
            cir.setReturnValue(-1);
        }
    }

    @Inject(method = "getHighestExplosionHeight()I", at = @At("HEAD"), cancellable = true)
    private void waterworld$highestExplosion(CallbackInfoReturnable<Integer> cir) {
        if (waterworld$layered) {
            for (int section = (OceanWorld.MAX_BUILD_Y >> 4) - 1; section >= (OceanWorld.MIN_BUILD_Y >> 4); section--) {
                if (hasExplosions(section)) {
                    cir.setReturnValue((section << 4) + 15);
                    return;
                }
            }
            cir.setReturnValue(-1);
        }
    }
}
