package com.carrot123.waterworld.item;

import com.carrot123.waterworld.block.OxygenMachineBlock;
import com.carrot123.waterworld.blockentity.OxygenMachineBlockEntity;
import com.carrot123.waterworld.worldgen.OceanPlacementLimit;
import com.carrot123.waterworld.worldgen.OceanWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.BlockSnapshot;

public final class OxygenMachineItem extends BlockItem {
    private final int size;

    public OxygenMachineItem(OxygenMachineBlock block, Properties properties) {
        super(block, properties);
        size = block.machineType().size();
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        CompoundTag tag = context.getItemInHand().getTag();
        if (tag != null && (tag.contains("BlockStateTag") || tag.contains("BlockEntityTag"))) {
            return InteractionResult.FAIL;
        }
        return super.place(context);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState mainState) {
        Level level = context.getLevel();
        BlockPos root = context.getClickedPos();
        List<BlockSnapshot> before = new ArrayList<>(size);
        for (int part = 0; part < size; part++) {
            BlockPos pos = OxygenMachineBlock.position(root, mainState, part);
            if (!available(level, pos)) {
                return false;
            }
            BlockPlaceContext local = part == 0 ? context : BlockPlaceContext.at(context, pos, context.getClickedFace());
            Player player = context.getPlayer();
            BlockState partState = mainState.setValue(OxygenMachineBlock.PART, part);
            if (!local.getClickedPos().equals(pos) || !local.canPlace() || !canPlace(local, partState)
                    || (player != null && (!level.mayInteract(player, pos)
                    || !player.mayUseItemAt(pos, local.getClickedFace(), context.getItemInHand())))) {
                return false;
            }
            before.add(BlockSnapshot.create(level.dimension(), level, pos, Block.UPDATE_CLIENTS));
        }

        int snapshotStart = level.capturedBlockSnapshots.size();
        int flags = level.captureBlockSnapshots ? Block.UPDATE_ALL : Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
        UUID identity = UUID.randomUUID();
        boolean placed = false;
        try {
            for (int part = 0; part < size; part++) {
                BlockPos pos = before.get(part).getPos();
                if (!level.setBlock(pos, mainState.setValue(OxygenMachineBlock.PART, part), flags)
                        || !(level.getBlockEntity(pos) instanceof OxygenMachineBlockEntity member)) {
                    return false;
                }
                if (!level.isClientSide) {
                    member.form(identity);
                }
            }
            placed = true;
            if (!level.isClientSide && !level.captureBlockSnapshots) {
                for (BlockSnapshot snapshot : before) {
                    level.blockUpdated(snapshot.getPos(), mainState.getBlock());
                }
            }
            return true;
        } finally {
            if (!placed) {
                restore(level, before, snapshotStart);
            }
        }
    }

    private static boolean available(Level level, BlockPos pos) {
        if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }
        if (level instanceof ServerLevel server) {
            return server.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) != null
                    && (!OceanWorld.isLayered(server) || pos.getY() <= OceanPlacementLimit.MAX_PLACEMENT_Y);
        }
        return level.hasChunkAt(pos);
    }

    private static void restore(Level level, List<BlockSnapshot> before, int snapshotStart) {
        boolean capturing = level.captureBlockSnapshots;
        boolean restoring = level.restoringBlockSnapshots;
        level.captureBlockSnapshots = false;
        level.restoringBlockSnapshots = true;
        try {
            for (int index = before.size() - 1; index >= 0; index--) {
                BlockSnapshot snapshot = before.get(index);
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
