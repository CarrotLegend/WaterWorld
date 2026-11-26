package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.worldgen.OceanWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerLevel.class)
public class OceanRainServerMixin {
    @Redirect(method = "tickChunk(Lnet/minecraft/world/level/chunk/LevelChunk;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"),
            require = 1, allow = 1)
    private Biome.Precipitation waterworld$rainOnBlocks(Biome biome, BlockPos pos) {
        return OceanWorld.precipitationAt((ServerLevel) (Object) this, biome, pos);
    }
}
