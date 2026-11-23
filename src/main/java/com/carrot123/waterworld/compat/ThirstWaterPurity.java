package com.carrot123.waterworld.compat;

import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.world.item.ItemStack;

final class ThirstWaterPurity {
    private ThirstWaterPurity() {
    }

    static ItemStack purify(ItemStack input) {
        ItemStack output = input.copyWithCount(1);
        if (!WaterPurity.isWaterFilledContainer(output)) {
            return ItemStack.EMPTY;
        }
        int purity = WaterPurity.getPurity(output);
        if (purity < WaterPurity.MIN_PURITY || purity >= WaterPurity.MAX_PURITY) {
            return ItemStack.EMPTY;
        }
        WaterPurity.addPurity(output, WaterPurity.MAX_PURITY);
        return WaterPurity.getPurity(output) == WaterPurity.MAX_PURITY ? output : ItemStack.EMPTY;
    }
}
