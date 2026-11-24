package com.carrot123.waterworld.oxygen;

import com.carrot123.waterworld.item.oxygen.OxygenTankItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class OxygenTankStorage {
    private static final String OXYGEN = "WaterWorldOxygen";

    private OxygenTankStorage() {
    }

    public static boolean isTank(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof OxygenTankItem;
    }

    public static double amount(ItemStack stack) {
        if (!(stack.getItem() instanceof OxygenTankItem tank)) {
            return 0.0D;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(OXYGEN, Tag.TAG_ANY_NUMERIC)) {
            return 0.0D;
        }
        double value = tag.getDouble(OXYGEN);
        return Double.isFinite(value) ? Math.max(0.0D, Math.min(tank.type().capacity(), value)) : 0.0D;
    }

    public static boolean canFill(ItemStack stack) {
        return stack.getItem() instanceof OxygenTankItem tank && amount(stack) < tank.type().capacity();
    }

    public static void setAmount(ItemStack stack, double value) {
        if (stack.getItem() instanceof OxygenTankItem tank) {
            double safe = Double.isFinite(value) ? Math.max(0.0D, Math.min(tank.type().capacity(), value)) : 0.0D;
            if (safe == 0.0D) {
                stack.removeTagKey(OXYGEN);
            } else {
                stack.getOrCreateTag().putDouble(OXYGEN, safe);
            }
        }
    }
}
