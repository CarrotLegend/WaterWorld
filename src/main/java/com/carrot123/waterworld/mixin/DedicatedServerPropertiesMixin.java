package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.worldgen.OceanWorld;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.world.level.levelgen.WorldDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServerProperties.class)
public class DedicatedServerPropertiesMixin {
    @Inject(method = "createDimensions", at = @At("RETURN"), cancellable = true)
    private void waterworld$newWorld(RegistryAccess access, CallbackInfoReturnable<WorldDimensions> cir) {
        cir.setReturnValue(OceanWorld.forceOverworld(access, cir.getReturnValue()));
    }
}
