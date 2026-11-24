package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.worldgen.OceanFlood;
import com.carrot123.waterworld.worldgen.OceanWorld;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Mixin(ChunkStatus.class)
public class ChunkStatusMixin {
    @Inject(method = "generate", at = @At("HEAD"))
    private void waterworld$flood(Executor executor, ServerLevel level, ChunkGenerator generator,
                                 StructureTemplateManager structures, ThreadedLevelLightEngine light,
                                 Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> full,
                                 List<ChunkAccess> chunks,
                                 CallbackInfoReturnable<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> cir) {
        if ((Object) this == ChunkStatus.LIGHT && level.dimension().equals(Level.OVERWORLD) && OceanWorld.isOcean(generator)) {
            ChunkAccess chunk = chunks.get(chunks.size() / 2);
            if (!chunk.getStatus().isOrAfter(ChunkStatus.LIGHT)) {
                OceanFlood.finish(chunk, OceanWorld.sea(generator), OceanWorld.isLayered(generator));
            }
        }
    }
}
