package com.carrot123.waterworld.menu;

import com.carrot123.waterworld.blockentity.SeawaterFilterBlockEntity;
import com.carrot123.waterworld.compat.FilterWater;
import com.carrot123.waterworld.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SeawaterFilterMenu extends AbstractContainerMenu {
    private static final int PLAYER_SLOTS_START = SeawaterFilterBlockEntity.SLOT_COUNT;
    private static final int HOTBAR_START = PLAYER_SLOTS_START + 27;
    private static final int PLAYER_SLOTS_END = HOTBAR_START + 9;
    private final Container container;
    private final ContainerData data;

    public SeawaterFilterMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(SeawaterFilterBlockEntity.SLOT_COUNT), new SimpleContainerData(2));
    }

    public SeawaterFilterMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.SEAWATER_FILTER.get(), id);
        checkContainerSize(container, SeawaterFilterBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, 2);
        this.container = container;
        this.data = data;

        addSlot(new Slot(container, SeawaterFilterBlockEntity.INPUT, 80, 22) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container instanceof SeawaterFilterBlockEntity
                        ? container.canPlaceItem(getContainerSlot(), stack) : FilterWater.isWaterContainer(stack);
            }
        });
        addSlot(new Slot(container, SeawaterFilterBlockEntity.OUTPUT, 80, 77) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, 9 + row * 9 + col, 8 + col * 18, 109 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 167));
        }
        addDataSlots(data);
    }

    public int progress() {
        return data.get(0);
    }

    public boolean isRunning() {
        return data.get(1) != 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < PLAYER_SLOTS_START) {
            if (!moveItemStackTo(stack, PLAYER_SLOTS_START, PLAYER_SLOTS_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (getSlot(SeawaterFilterBlockEntity.INPUT).mayPlace(stack)) {
            if (!moveItemStackTo(stack, SeawaterFilterBlockEntity.INPUT, SeawaterFilterBlockEntity.OUTPUT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < HOTBAR_START) {
            if (!moveItemStackTo(stack, HOTBAR_START, PLAYER_SLOTS_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, PLAYER_SLOTS_START, HOTBAR_START, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }
}
