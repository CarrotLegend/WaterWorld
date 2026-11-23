package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.OceanWorld;
import mcjty.lostcities.config.ProfileSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ProfileSetup.class, remap = false)
public class ProfileSetupMixin {
    @Inject(method = "setupProfiles()V", at = @At("RETURN"))
    private static void waterworld$setup(CallbackInfo ci) {
        OceanWorld.setupProfile();
    }
}
