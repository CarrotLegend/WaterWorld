package com.carrot123.waterworld.compat;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.fml.ModList;

public final class FilterWater {
    private FilterWater() {
    }

    public static boolean isWaterContainer(ItemStack stack) {
        return stack.is(Items.WATER_BUCKET) || (stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER);
    }

    public static ItemStack purifiedOutput(ItemStack input) {
        if (!isWaterContainer(input) || ModList.get().getModContainerById("thirst")
                .filter(mod -> mod.getModInfo().getVersion().toString().equals("1.20.1-1.4.0")).isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ThirstWaterPurity.purify(input);
    }
}
