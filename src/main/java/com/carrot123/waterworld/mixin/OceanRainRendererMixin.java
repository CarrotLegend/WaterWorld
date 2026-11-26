package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.worldgen.OceanWorld;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public class OceanRainRendererMixin {
    @Shadow
    private ClientLevel level;

    @Redirect(method = "renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"),
            require = 1, allow = 1)
    private Biome.Precipitation waterworld$renderOceanRain(Biome biome, BlockPos pos) {
        return OceanWorld.precipitationAt(level, biome, pos);
    }

    @Redirect(method = "tickRain(Lnet/minecraft/client/Camera;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"),
            require = 1, allow = 1)
    private Biome.Precipitation waterworld$tickOceanRain(Biome biome, BlockPos pos) {
        return OceanWorld.precipitationAt(level, biome, pos);
    }
}
