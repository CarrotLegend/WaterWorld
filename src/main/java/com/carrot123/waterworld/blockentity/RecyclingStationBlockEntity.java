package com.carrot123.waterworld.blockentity;

import com.carrot123.waterworld.block.RecyclingStationBlock;
import com.carrot123.waterworld.block.RecyclingStationBlock.Part;
import com.carrot123.waterworld.registry.ModBlockEntities;
import com.carrot123.waterworld.registry.ModItems;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

public class RecyclingStationBlockEntity extends BlockEntity {
    private static final String STORED_ITEM = "StoredItem";
    private static final String STRUCTURE = "Structure";
    private ItemStack storedItem = ItemStack.EMPTY;
    @Nullable private UUID structure;
    private boolean retiring;
    private boolean removalOrigin;

    public RecyclingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RECYCLING_STATION.get(), pos, state);
    }

    public ItemStack getStoredItem() {
        return storedItem.copy();
    }

    public void form(UUID id) {
        structure = id;
        setChanged();
    }

    @Nullable
    public RecyclingStationBlockEntity controller() {
        if (isRemoved() || retiring || level == null) {
            return null;
        }
        BlockState state = getBlockState();
        Part part = state.getValue(RecyclingStationBlock.PART);
        if (part == Part.LEGACY) {
            return this;
        }
        BlockPos otherPos = RecyclingStationBlock.partnerPos(worldPosition, state);
        if (level instanceof ServerLevel server) {
            RecyclingStationRemovalData removals = RecyclingStationRemovalData.get(server);
            if (structure == null || removals.contains(structure, worldPosition) || removals.contains(structure, otherPos)) {
                return null;
            }
            LevelChunk chunk = loadedChunk(server, otherPos);
            if (chunk == null) {
                return part == Part.MAIN ? this : null;
            }
            RecyclingStationBlockEntity member = partner(chunk, otherPos, state);
            return member == null ? null : part == Part.MAIN ? this : member;
        }
        if (part == Part.MAIN) {
            return this;
        }
        if (level.hasChunkAt(otherPos) && RecyclingStationBlock.isPartner(state, level.getBlockState(otherPos))
                && level.getBlockEntity(otherPos) instanceof RecyclingStationBlockEntity member) {
            return member;
        }
        return null;
    }

    private static LevelChunk loadedChunk(ServerLevel server, BlockPos pos) {
        return server.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
    }

    @Nullable
    private RecyclingStationBlockEntity partner(LevelChunk chunk, BlockPos pos, BlockState state) {
        if (RecyclingStationBlock.isPartner(state, chunk.getBlockState(pos))
                && chunk.getBlockEntity(pos) instanceof RecyclingStationBlockEntity member
                && structure != null && structure.equals(member.structure) && !member.isRemoved() && !member.retiring) {
            return member;
        }
        return null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RecyclingStationBlockEntity member) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        if (member.structure == null || member.retiring || RecyclingStationRemovalData.get(server).contains(member.structure, pos)) {
            member.removePart(server, pos);
            return;
        }
        BlockPos otherPos = RecyclingStationBlock.partnerPos(pos, state);
        LevelChunk chunk = loadedChunk(server, otherPos);
        if (chunk != null && member.partner(chunk, otherPos, state) == null) {
            member.removePart(server, pos);
        }
    }

    public boolean mayDrop(ServerLevel server) {
        return structure != null && (removalOrigin || (!retiring
                && !RecyclingStationRemovalData.get(server).contains(structure, worldPosition)));
    }

    public void dismantle(ServerLevel server, BlockState state) {
        if (structure == null || retiring) {
            return;
        }
        RecyclingStationRemovalData removals = RecyclingStationRemovalData.get(server);
        retiring = true;
        if (removals.contains(structure, worldPosition)) {
            removals.complete(structure, worldPosition);
            return;
        }
        removalOrigin = true;
        BlockPos otherPos = RecyclingStationBlock.partnerPos(worldPosition, state);
        LevelChunk chunk = loadedChunk(server, otherPos);
        if (chunk == null) {
            removals.enqueue(structure, otherPos);
            return;
        }
        RecyclingStationBlockEntity member = partner(chunk, otherPos, state);
        if (member != null) {
            removals.enqueue(structure, otherPos);
            member.removePart(server, otherPos);
        }
    }

    private void removePart(ServerLevel server, BlockPos pos) {
        server.setBlock(pos, getBlockState().getFluidState().createLegacyBlock(),
                Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
    }

    @Override
    public AABB getRenderBoundingBox() {
        AABB own = new AABB(worldPosition);
        return getBlockState().getValue(RecyclingStationBlock.PART) == Part.MAIN
                ? own.minmax(new AABB(RecyclingStationBlock.partnerPos(worldPosition, getBlockState()))).expandTowards(0, 1, 0)
                : own;
    }

    public boolean isEmpty() {
        return storedItem.isEmpty();
    }

    public boolean insertScrap(ItemStack stack) {
        if (level == null || level.isClientSide || getBlockState().getValue(RecyclingStationBlock.PART) == Part.SECONDARY
                || !isEmpty() || !stack.is(ModItems.SCRAP_IRON.get())) {
            return false;
        }
        storedItem = stack.copyWithCount(1);
        sync();
        return true;
    }

    public ItemStack takeScrap() {
        if (level == null || level.isClientSide || isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = clearForRemoval();
        sync();
        return result;
    }

    public ItemStack clearForRemoval() {
        ItemStack result = storedItem;
        storedItem = ItemStack.EMPTY;
        return result;
    }

    private void sync() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (structure != null) {
            tag.putUUID(STRUCTURE, structure);
        }
        if (getBlockState().getValue(RecyclingStationBlock.PART) != Part.SECONDARY && !isEmpty()) {
            tag.put(STORED_ITEM, storedItem.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        structure = tag.hasUUID(STRUCTURE) ? tag.getUUID(STRUCTURE) : null;
        ItemStack loaded = getBlockState().getValue(RecyclingStationBlock.PART) == Part.SECONDARY
                ? ItemStack.EMPTY : ItemStack.of(tag.getCompound(STORED_ITEM));
        storedItem = loaded.is(ModItems.SCRAP_IRON.get()) && !loaded.isEmpty()
                ? loaded.copyWithCount(1) : ItemStack.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
