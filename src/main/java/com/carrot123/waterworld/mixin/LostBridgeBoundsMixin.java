package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.worldgen.LayeredCityInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.Orientation;
import mcjty.lostcities.worldgen.street.HierarchicalBridgePlanner;
import mcjty.lostcities.worldgen.street.PlannedBridgeInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = HierarchicalBridgePlanner.class, remap = false)
public class LostBridgeBoundsMixin {
    @Inject(method = "getBridgeInfo(Lmcjty/lostcities/worldgen/lost/BuildingInfo;Lmcjty/lostcities/worldgen/lost/Orientation;)Lmcjty/lostcities/worldgen/street/PlannedBridgeInfo;",
            at = @At("RETURN"), cancellable = true)
    private static void waterworld$restrictBridgeSpan(BuildingInfo source, Orientation orientation,
                                                      CallbackInfoReturnable<PlannedBridgeInfo> cir) {
        PlannedBridgeInfo bridge = cir.getReturnValue();
        if (source.provider instanceof LayeredCityInfo info && bridge != null
                && !info.allowsSpan(bridge.minimumEndpoint(), bridge.maximumEndpoint())) {
            cir.setReturnValue(null);
        }
    }
}
