package com.carrot123.waterworld.menu;

import com.carrot123.waterworld.oxygen.ChestplateOxygenInventory;
import com.carrot123.waterworld.oxygen.OxygenTankStorage;
import com.carrot123.waterworld.registry.ModMenus;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class OxygenTankMenu extends AbstractContainerMenu {
    private static final int PLAYER_START = ChestplateOxygenInventory.SIZE;
    private static final int HOTBAR_START = PLAYER_START + 27;
    private static final int PLAYER_END = HOTBAR_START + 9;
    private final Inventory inventory;
    private final int lockedHotbar;
    @Nullable private final ItemStack chestplate;

    public OxygenTankMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readUnsignedByte(), null);
    }

    public OxygenTankMenu(int id, Inventory inventory, int lockedHotbar, @Nullable ItemStack chestplate) {
        super(ModMenus.OXYGEN_TANKS.get(), id);
        if (!Inventory.isHotbarSlot(lockedHotbar)) {
            throw new IllegalArgumentException("Invalid chestplate hotbar slot");
        }
        this.inventory = inventory;
        this.lockedHotbar = lockedHotbar;
        this.chestplate = chestplate;
        SimpleContainer tanks = new SimpleContainer(ChestplateOxygenInventory.SIZE);
        if (chestplate != null) {
            NonNullList<ItemStack> initial = ChestplateOxygenInventory.read(chestplate);
            for (int slot = 0; slot < initial.size(); slot++) {
                tanks.setItem(slot, initial.get(slot));
            }
            tanks.addListener(container -> writeTanks(tanks, chestplate));
        }
        for (int slot = 0; slot < ChestplateOxygenInventory.SIZE; slot++) {
            addSlot(new Slot(tanks, slot, 40 + slot * 80, 28) {
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
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, 9 + row * 9 + col, 8 + col * 18, 76 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            int x = 8 + col * 18;
            addSlot(col == lockedHotbar ? lockedSlot(inventory, col, x) : new Slot(inventory, col, x, 134));
        }
    }

    private static Slot lockedSlot(Inventory inventory, int index, int x) {
        return new Slot(inventory, index, x, 134) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }
        };
    }

    private void writeTanks(SimpleContainer tanks, ItemStack bound) {
        if (!stillValid(inventory.player)) {
            return;
        }
        NonNullList<ItemStack> contents = NonNullList.withSize(ChestplateOxygenInventory.SIZE, ItemStack.EMPTY);
        for (int slot = 0; slot < contents.size(); slot++) {
            contents.set(slot, tanks.getItem(slot));
        }
        ChestplateOxygenInventory.write(bound, contents);
        inventory.setChanged();
    }

    public boolean locks(ItemStack stack) {
        return chestplate == null ? inventory.getItem(lockedHotbar) == stack : chestplate == stack;
    }

    @Override
    public boolean stillValid(Player player) {
        return chestplate == null || (inventory.player == player && player.isAlive() && !player.isSpectator()
                && inventory.selected == lockedHotbar && inventory.getItem(lockedHotbar) == chestplate
                && ChestplateOxygenInventory.isChestplate(chestplate));
    }

    @Override
    public void clicked(int slot, int button, ClickType type, Player player) {
        if (!stillValid(player) || slot == HOTBAR_START + lockedHotbar
                || (type == ClickType.SWAP && button == lockedHotbar)) {
            return;
        }
        super.clicked(slot, button, type, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || index == HOTBAR_START + lockedHotbar || !stillValid(player)) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < PLAYER_START) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (OxygenTankStorage.isTank(stack)) {
            if (!moveItemStackTo(stack, 0, PLAYER_START, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < HOTBAR_START) {
            if (!moveItemStackTo(stack, HOTBAR_START, PLAYER_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, PLAYER_START, HOTBAR_START, false)) {
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
