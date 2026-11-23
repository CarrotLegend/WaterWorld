package com.carrot123.waterworld.blockentity;

import com.carrot123.waterworld.block.SeawaterFilterBlock;
import com.carrot123.waterworld.compat.FilterWater;
import com.carrot123.waterworld.menu.SeawaterFilterMenu;
import com.carrot123.waterworld.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SeawaterFilterBlockEntity extends BaseContainerBlockEntity {
    public static final int INPUT = 0;
    public static final int OUTPUT = 1;
    public static final int SLOT_COUNT = 2;
    public static final int PROCESS_TICKS = 200;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private ItemStack workInput = ItemStack.EMPTY;
    private int progress;
    private boolean running;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> running ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public SeawaterFilterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SEAWATER_FILTER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SeawaterFilterBlockEntity filter) {
        if (!level.isClientSide && SeawaterFilterBlock.hasUpper(level, pos, state)) {
            filter.process();
        }
    }

    private void process() {
        if (progress == PROCESS_TICKS) {
            progress = 0;
            super.setChanged();
        }

        ItemStack input = items.get(INPUT);
        ItemStack result = FilterWater.purifiedOutput(input);
        if (result.isEmpty()) {
            if (progress != 0 || !workInput.isEmpty()) {
                resetProgress();
                super.setChanged();
            }
            running = false;
            return;
        }

        if (!ItemStack.isSameItemSameTags(input, workInput)) {
            resetProgress();
        }
        if (!canOutput(result)) {
            running = false;
            return;
        }

        if (workInput.isEmpty()) {
            workInput = input.copyWithCount(1);
        }
        running = true;
        progress++;
        if (progress == PROCESS_TICKS) {
            ItemStack output = items.get(OUTPUT);
            if (output.isEmpty()) {
                items.set(OUTPUT, result);
            } else {
                output.grow(1);
            }
            input.shrink(1);
            if (input.isEmpty()) {
                items.set(INPUT, ItemStack.EMPTY);
            }
            workInput = ItemStack.EMPTY;
            running = false;
        }
        super.setChanged();
    }

    private boolean canOutput(ItemStack result) {
        ItemStack output = items.get(OUTPUT);
        return output.isEmpty() || (ItemStack.isSameItemSameTags(output, result)
                && output.getCount() < Math.min(getMaxStackSize(), output.getMaxStackSize()));
    }

    private void resetProgress() {
        workInput = ItemStack.EMPTY;
        progress = 0;
        running = false;
    }

    @Override
    public void setChanged() {
        if (!workInput.isEmpty() && !ItemStack.isSameItemSameTags(items.get(INPUT), workInput)) {
            resetProgress();
        }
        super.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        if (!workInput.isEmpty()) {
            tag.putInt("Progress", progress);
            tag.put("WorkInput", workInput.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        resetProgress();
        if (tag.contains("WorkInput", Tag.TAG_COMPOUND)) {
            ItemStack savedInput = ItemStack.of(tag.getCompound("WorkInput"));
            if (!savedInput.isEmpty() && ItemStack.isSameItemSameTags(items.get(INPUT), savedInput)) {
                progress = Mth.clamp(tag.getInt("Progress"), 0, PROCESS_TICKS - 1);
                workInput = savedInput.copyWithCount(1);
            }
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.waterworld.seawater_filter");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new SeawaterFilterMenu(id, inventory, this, data);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return items.get(INPUT).isEmpty() && items.get(OUTPUT).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, count);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = ContainerHelper.takeItem(items, slot);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (!stack.isEmpty()) {
            stack.setCount(Math.min(stack.getCount(), Math.min(getMaxStackSize(), stack.getMaxStackSize())));
        }
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == INPUT && !FilterWater.purifiedOutput(stack).isEmpty();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && player.level() == level && level.getBlockEntity(worldPosition) == this
                && SeawaterFilterBlock.hasUpper(level, worldPosition, getBlockState())
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
        resetProgress();
        setChanged();
    }
}
