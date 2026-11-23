package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.worldgen.OceanWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {
    @Redirect(method = "postProcessGeneration", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean waterworld$shapeUpdate(Level level, BlockPos pos, BlockState state, int flags) {
        if (pos.getY() <= OceanWorld.SEA && state.isAir() && OceanWorld.isOcean(level)) {
            return level.setBlock(pos, Blocks.WATER.defaultBlockState(), flags);
        }
        return level.setBlock(pos, state, flags);
    }
}
