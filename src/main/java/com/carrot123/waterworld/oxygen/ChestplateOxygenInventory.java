package com.carrot123.waterworld.oxygen;

import com.carrot123.waterworld.item.armor.DivingSuitItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public final class ChestplateOxygenInventory {
    public static final int SIZE = 2;
    private static final String TANKS = "WaterWorldOxygenTanks";

    private ChestplateOxygenInventory() {
    }

    public static boolean isChestplate(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof DivingSuitItem suit
                && suit.getType() == ArmorItem.Type.CHESTPLATE;
    }

    public static boolean hasStoredTanks(ItemStack chestplate) {
        return chestplate.getTag() != null && chestplate.getTag().contains(TANKS, Tag.TAG_COMPOUND);
    }

    public static NonNullList<ItemStack> read(ItemStack chestplate) {
        NonNullList<ItemStack> slots = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        if (isChestplate(chestplate) && hasStoredTanks(chestplate)) {
            ContainerHelper.loadAllItems(chestplate.getTag().getCompound(TANKS), slots);
            for (int slot = 0; slot < SIZE; slot++) {
                ItemStack stack = slots.get(slot);
                slots.set(slot, OxygenTankStorage.isTank(stack) ? stack.copyWithCount(1) : ItemStack.EMPTY);
            }
        }
        return slots;
    }

    public static void write(ItemStack chestplate, NonNullList<ItemStack> slots) {
        if (!isChestplate(chestplate)) {
            return;
        }
        if (slots.stream().allMatch(ItemStack::isEmpty)) {
            chestplate.removeTagKey(TANKS);
        } else {
            chestplate.getOrCreateTag().put(TANKS, ContainerHelper.saveAllItems(new CompoundTag(), slots));
        }
    }

    public static NonNullList<ItemStack> takeAll(ItemStack chestplate) {
        NonNullList<ItemStack> contents = read(chestplate);
        chestplate.removeTagKey(TANKS);
        return contents;
    }
}
