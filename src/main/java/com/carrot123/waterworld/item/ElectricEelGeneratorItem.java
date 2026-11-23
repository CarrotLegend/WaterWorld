package com.carrot123.waterworld.item;

import com.carrot123.waterworld.block.ElectricEelGeneratorBlock;
import com.carrot123.waterworld.blockentity.ElectricEelGeneratorBlockEntity;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.BlockSnapshot;

public class ElectricEelGeneratorItem extends BlockItem {
    public ElectricEelGeneratorItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (context.getItemInHand().getTag() != null
                && (context.getItemInHand().getTag().contains("BlockStateTag")
                || context.getItemInHand().getTag().contains("BlockEntityTag"))) {
            return InteractionResult.FAIL;
        }
        return super.place(context);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState mainState) {
        Level level = context.getLevel();
        BlockPos mainPos = context.getClickedPos();
        BlockPos secondaryPos = ElectricEelGeneratorBlock.partnerPos(mainPos, mainState);
        BlockState secondaryState = mainState.setValue(ElectricEelGeneratorBlock.SECONDARY, true);
        if (!canPlaceBoth(context, mainState, secondaryPos, secondaryState)) {
            return false;
        }

        if (!(level instanceof ServerLevel server)) {
            int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
            BlockState previous = level.getBlockState(mainPos);
            if (!level.setBlock(mainPos, mainState, flags)) {
                return false;
            }
            if (!level.setBlock(secondaryPos, secondaryState, flags)) {
                level.setBlock(mainPos, previous, flags);
                return false;
            }
            return true;
        }

        int snapshotStart = level.capturedBlockSnapshots.size();
        List<BlockSnapshot> before = List.of(
                BlockSnapshot.create(level.dimension(), level, mainPos, Block.UPDATE_CLIENTS),
                BlockSnapshot.create(level.dimension(), level, secondaryPos, Block.UPDATE_CLIENTS));
        int flags = level.captureBlockSnapshots ? Block.UPDATE_ALL : Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
        boolean placed = false;
        try {
            if (!level.setBlock(mainPos, mainState, flags) || !level.setBlock(secondaryPos, secondaryState, flags)
                    || !(level.getBlockEntity(mainPos) instanceof ElectricEelGeneratorBlockEntity main)
                    || !(level.getBlockEntity(secondaryPos) instanceof ElectricEelGeneratorBlockEntity secondary)) {
                return false;
            }
            UUID pair = UUID.randomUUID();
            main.formPair(pair);
            secondary.formPair(pair);
            placed = true;
            if (!level.captureBlockSnapshots) {
                server.blockUpdated(mainPos, mainState.getBlock());
                server.blockUpdated(secondaryPos, secondaryState.getBlock());
            }
            return true;
        } finally {
            if (!placed) {
                restore(level, before, snapshotStart);
            }
        }
    }

    private boolean canPlaceBoth(BlockPlaceContext context, BlockState mainState, BlockPos secondaryPos,
                                 BlockState secondaryState) {
        Level level = context.getLevel();
        if (!isAvailable(level, context.getClickedPos()) || !isAvailable(level, secondaryPos)) {
            return false;
        }
        BlockPlaceContext secondary = BlockPlaceContext.at(context, secondaryPos, context.getClickedFace());
        Player player = context.getPlayer();
        if (player != null && (!level.mayInteract(player, context.getClickedPos()) || !level.mayInteract(player, secondaryPos)
                || !player.mayUseItemAt(secondaryPos, secondary.getClickedFace(), context.getItemInHand()))) {
            return false;
        }
        return context.canPlace() && canPlace(context, mainState)
                && secondary.getClickedPos().equals(secondaryPos) && secondary.canPlace()
                && canPlace(secondary, secondaryState);
    }

    private static boolean isAvailable(Level level, BlockPos pos) {
        return level.isInWorldBounds(pos) && level.getWorldBorder().isWithinBounds(pos)
                && (level instanceof ServerLevel server
                ? server.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) != null
                : level.hasChunkAt(pos));
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
