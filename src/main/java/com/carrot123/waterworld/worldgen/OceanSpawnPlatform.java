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
    private static final BlockPos SPAWN = new BlockPos(8, OceanWorld.SEA + 1, 8);
    private static final float ANGLE = 0.0F;

    public static void create(LevelEvent.CreateSpawnPosition event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !OceanWorld.isOcean(level)) {
            return;
        }
        if (level.getDataStorage().get(tag -> new OceanSpawnPlatform(), DATA_NAME) == null) {
            level.getChunk(0, 0);
            for (int x = 7; x <= 9; x++) {
                for (int z = 7; z <= 9; z++) {
                    BlockPos feet = new BlockPos(x, SPAWN.getY(), z);
                    level.setBlock(feet.below(), Blocks.OAK_PLANKS.defaultBlockState(), Block.UPDATE_ALL);
                    level.setBlock(feet, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    level.setBlock(feet.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            OceanSpawnPlatform data = new OceanSpawnPlatform();
            level.getDataStorage().set(DATA_NAME, data);
            data.setDirty();
        }
        event.getSettings().setSpawn(SPAWN, ANGLE);
        event.setCanceled(true);
    }

    public static void placeFirstPlayer(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (OceanWorld.isOcean(level) && SPAWN.equals(level.getSharedSpawnPos())
                && level.getDataStorage().get(tag -> new OceanSpawnPlatform(), DATA_NAME) != null
                && level.getBlockState(SPAWN.below()).is(Blocks.OAK_PLANKS)
                && level.isEmptyBlock(SPAWN) && level.isEmptyBlock(SPAWN.above())) {
            player.moveTo(SPAWN, ANGLE, 0.0F);
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return tag;
    }
}
