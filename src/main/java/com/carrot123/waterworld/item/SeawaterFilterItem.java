package com.carrot123.waterworld.item;

import com.carrot123.waterworld.block.SeawaterFilterBlock;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.BlockSnapshot;

public class SeawaterFilterItem extends BlockItem {
    public SeawaterFilterItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (context.getItemInHand().getTag() != null && (context.getItemInHand().getTag().contains("BlockStateTag")
                || context.getItemInHand().getTag().contains("BlockEntityTag"))) {
            return InteractionResult.FAIL;
        }
        return super.place(context);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState lowerState) {
        Level level = context.getLevel();
        BlockPos lower = context.getClickedPos();
        BlockPos upper = lower.above();
        if (!level.isInWorldBounds(upper) || !level.getWorldBorder().isWithinBounds(upper)) {
            return false;
        }
        Player player = context.getPlayer();
        if (player != null && (!level.mayInteract(player, upper)
                || !player.mayUseItemAt(upper, Direction.UP, context.getItemInHand()))) {
            return false;
        }
        BlockPlaceContext upperContext = BlockPlaceContext.at(context, upper, Direction.UP);
        BlockState upperState = lowerState.setValue(SeawaterFilterBlock.HALF, DoubleBlockHalf.UPPER)
                .setValue(SeawaterFilterBlock.WATERLOGGED, level.getFluidState(upper).getType() == Fluids.WATER);
        if (!upperContext.getClickedPos().equals(upper) || !upperContext.canPlace() || !canPlace(upperContext, upperState)) {
            return false;
        }

        int quietFlags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
        if (level.isClientSide) {
            BlockState previous = level.getBlockState(lower);
            if (!level.setBlock(lower, lowerState, quietFlags)) {
                return false;
            }
            if (!level.setBlock(upper, upperState, quietFlags)) {
                level.setBlock(lower, previous, quietFlags);
                return false;
            }
            return true;
        }

        int snapshotStart = level.capturedBlockSnapshots.size();
        List<BlockSnapshot> before = List.of(
                BlockSnapshot.create(level.dimension(), level, lower, Block.UPDATE_CLIENTS),
                BlockSnapshot.create(level.dimension(), level, upper, Block.UPDATE_CLIENTS));
        int flags = level.captureBlockSnapshots ? Block.UPDATE_ALL : quietFlags;
        boolean placed = false;
        try {
            placed = level.setBlock(lower, lowerState, flags) && level.setBlock(upper, upperState, flags);
            if (placed && !level.captureBlockSnapshots) {
                level.blockUpdated(lower, lowerState.getBlock());
                level.blockUpdated(upper, upperState.getBlock());
            }
            return placed;
        } finally {
            if (!placed) {
                restore(level, before, snapshotStart);
            }
        }
    }

    private static void restore(Level level, List<BlockSnapshot> before, int snapshotStart) {
        boolean capturing = level.captureBlockSnapshots;
        boolean restoring = level.restoringBlockSnapshots;
        level.captureBlockSnapshots = false;
        level.restoringBlockSnapshots = true;
        try {
            for (int i = before.size() - 1; i >= 0; i--) {
                BlockSnapshot snapshot = before.get(i);
                if (snapshot.getCurrentBlock() != snapshot.getReplacedBlock()) {
                    snapshot.restore(true, false);
                }
            }
        } finally {
            level.capturedBlockSnapshots.subList(snapshotStart, level.capturedBlockSnapshots.size()).clear();
            level.restoringBlockSnapshots = restoring;
            level.captureBlockSnapshots = capturing;
        }
    }
}
