package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.worldgen.OceanWorld;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {
    @Shadow @Final private WorldCreationUiState uiState;

    @Inject(method = "onCreate", at = @At("HEAD"))
    private void waterworld$newWorld(CallbackInfo ci) {
        uiState.updateDimensions(OceanWorld::forceOverworld);
    }
}
