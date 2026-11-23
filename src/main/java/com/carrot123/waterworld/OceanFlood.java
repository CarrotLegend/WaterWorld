package com.carrot123.waterworld;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;

public class OceanFlood {
    public static void finish(ChunkAccess chunk) {
        settleGravity(chunk);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState water = Blocks.WATER.defaultBlockState();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        for (int y = chunk.getMinBuildHeight(); y <= OceanWorld.SEA; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    pos.set(minX + x, y, minZ + z);
                    BlockState state = chunk.getBlockState(pos);
                    if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                        boolean waterlogged = !state.hasProperty(BlockStateProperties.SLAB_TYPE)
                                || state.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE;
                        if (state.getValue(BlockStateProperties.WATERLOGGED) != waterlogged) {
                            chunk.setBlockState(pos, state.setValue(BlockStateProperties.WATERLOGGED, waterlogged), false);
                        }
                    } else if (!state.hasBlockEntity() && isFloodable(state)) {
                        if (!state.equals(water)) {
                            chunk.setBlockState(pos, water, false);
                        }
                    }
                }
            }
        }
        chunk.initializeLightSources();
    }

    private static void settleGravity(ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos below = new BlockPos.MutableBlockPos();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        for (int x = minX; x < minX + 16; x++) {
            for (int z = minZ; z < minZ + 16; z++) {
                for (int y = chunk.getMinBuildHeight() + 1; y <= OceanWorld.SEA; y++) {
                    pos.set(x, y, z);
                    BlockState state = chunk.getBlockState(pos);
                    if (!(state.getBlock() instanceof FallingBlock)) {
                        continue;
                    }
                    int target = y;
                    while (target > chunk.getMinBuildHeight()) {
                        below.set(x, target - 1, z);
                        if (!FallingBlock.isFree(chunk.getBlockState(below))) {
                            break;
                        }
                        target--;
                    }
                    if (target != y) {
                        chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                        below.set(x, target, z);
                        chunk.setBlockState(below, state, false);
                    }
                }
            }
        }
    }

    private static boolean isFloodable(BlockState state) {
        return state.isAir() || state.getBlock() instanceof LiquidBlock
                || state.getBlock() instanceof BushBlock || state.getBlock() instanceof TorchBlock
                || state.getBlock() instanceof RedStoneWireBlock || state.getBlock() instanceof DiodeBlock
                || state.getBlock() instanceof VineBlock || state.getBlock() instanceof BaseFireBlock
                || state.is(BlockTags.CANDLES) || state.is(Blocks.SNOW) || state.is(Blocks.TRIPWIRE)
                || state.is(Blocks.TRIPWIRE_HOOK) || state.is(Blocks.COBWEB)
                || state.is(Blocks.LADDER) || state.is(Blocks.SUGAR_CANE) || state.is(Blocks.CACTUS);
    }
}
