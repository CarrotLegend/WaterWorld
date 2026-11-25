package com.carrot123.waterworld.menu;

import com.carrot123.waterworld.blockentity.OxygenMachineBlockEntity;
import com.carrot123.waterworld.oxygen.OxygenMachineType;
import com.carrot123.waterworld.oxygen.OxygenTankStorage;
import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class OxygenMachineMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = 4;
    private final OxygenMachineType type;
    private final ContainerData data;
    @Nullable private final OxygenMachineBlockEntity core;
    @Nullable private final OxygenMachineBlockEntity openedPart;

    public OxygenMachineMenu(int id, Inventory inventory, OxygenMachineType type) {
        this(id, inventory, type, new SimpleContainer(type.size()), null, null);
    }

    public OxygenMachineMenu(int id, Inventory inventory, OxygenMachineType type, Container tanks,
                             @Nullable OxygenMachineBlockEntity core, @Nullable OxygenMachineBlockEntity openedPart) {
        super(type.menuType(), id);
        this.type = type;
        this.core = core;
        this.openedPart = openedPart;
        data = core == null ? new SimpleContainerData(DATA_COUNT) : new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> core.storedEnergy() & 0xffff;
                    case 1 -> core.storedEnergy() >>> 16;
                    case 2 -> core.storedWater();
                    case 3 -> core.workStatus();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
        for (int slot = 0; slot < type.size(); slot++) {
            int x = 61 + slot % 2 * 38;
            int y = type.usesWater() ? 22 + slot / 2 * 24 : 31;
            addSlot(new Slot(tanks, slot, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return OxygenTankStorage.isTank(stack);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, 9 + row * 9 + column, 8 + column * 18, 96 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 154));
        }
        addDataSlots(data);
    }

    public OxygenMachineType machineType() {
        return type;
    }

    public int energy() {
        return (data.get(1) & 0xffff) << 16 | data.get(0) & 0xffff;
    }

    public int water() {
        return data.get(2);
    }

    public int workStatus() {
        return data.get(3);
    }

    @Override
    public boolean stillValid(Player player) {
        return openedPart == null || (player.isAlive() && player.level() == openedPart.getLevel()
                && player.distanceToSqr(openedPart.getBlockPos().getX() + 0.5,
                openedPart.getBlockPos().getY() + 0.5, openedPart.getBlockPos().getZ() + 0.5) <= 64.0
                && openedPart.core() == core);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !stillValid(player)) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        int machineEnd = type.size();
        int hotbar = machineEnd + 27;
        if (index < machineEnd) {
            if (!moveItemStackTo(stack, machineEnd, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (OxygenTankStorage.isTank(stack)) {
            if (!moveItemStackTo(stack, 0, machineEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < hotbar) {
            if (!moveItemStackTo(stack, hotbar, slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, machineEnd, hotbar, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return result;
    }
}
