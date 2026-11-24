package com.carrot123.waterworld.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.level.LevelEvent;

public class OceanSpawnPlatform extends SavedData {
    private static final String DATA_NAME = "waterworld_spawn_platform";
    private static final float ANGLE = 0.0F;

    public static void create(LevelEvent.CreateSpawnPosition event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !OceanWorld.isOcean(level)) {
            return;
        }
        BlockPos spawn = spawn(level);
        if (level.getDataStorage().get(tag -> new OceanSpawnPlatform(), DATA_NAME) == null) {
            level.getChunk(0, 0);
            for (int x = 7; x <= 9; x++) {
                for (int z = 7; z <= 9; z++) {
                    BlockPos feet = new BlockPos(x, spawn.getY(), z);
                    level.setBlock(feet.below(), Blocks.OAK_PLANKS.defaultBlockState(), Block.UPDATE_ALL);
                    level.setBlock(feet, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    level.setBlock(feet.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            OceanSpawnPlatform data = new OceanSpawnPlatform();
            level.getDataStorage().set(DATA_NAME, data);
            data.setDirty();
        }
        event.getSettings().setSpawn(spawn, ANGLE);
        event.setCanceled(true);
    }

    public static void placeFirstPlayer(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos spawn = spawn(level);
        if (OceanWorld.isOcean(level) && spawn.equals(level.getSharedSpawnPos())
                && level.getDataStorage().get(tag -> new OceanSpawnPlatform(), DATA_NAME) != null
                && level.getBlockState(spawn.below()).is(Blocks.OAK_PLANKS)
                && level.isEmptyBlock(spawn) && level.isEmptyBlock(spawn.above())) {
            player.moveTo(spawn, ANGLE, 0.0F);
        }
    }

    private static BlockPos spawn(ServerLevel level) {
        return new BlockPos(8, OceanWorld.sea(level) + 1, 8);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return tag;
    }
}
