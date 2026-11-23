package com.carrot123.waterworld;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RecyclingStationBlockEntity extends BlockEntity {
    private static final String STORED_ITEM = "StoredItem";
    private ItemStack storedItem = ItemStack.EMPTY;

    public RecyclingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RECYCLING_STATION.get(), pos, state);
    }

    public ItemStack getStoredItem() {
        return storedItem.copy();
    }

    boolean isEmpty() {
        return storedItem.isEmpty();
    }

    boolean insertScrap(ItemStack stack) {
        if (level == null || level.isClientSide || !isEmpty() || !stack.is(ModItems.SCRAP_IRON.get())) {
            return false;
        }
        storedItem = stack.copyWithCount(1);
        sync();
        return true;
    }

    ItemStack takeScrap() {
        if (level == null || level.isClientSide || isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = clearForRemoval();
        sync();
        return result;
    }

    ItemStack clearForRemoval() {
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
        if (!isEmpty()) {
            tag.put(STORED_ITEM, storedItem.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ItemStack loaded = ItemStack.of(tag.getCompound(STORED_ITEM));
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
