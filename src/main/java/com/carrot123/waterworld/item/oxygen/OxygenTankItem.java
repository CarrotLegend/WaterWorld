package com.carrot123.waterworld.item.oxygen;

import com.carrot123.waterworld.oxygen.OxygenTankStorage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class OxygenTankItem extends Item {
    private static final String TOOLTIP = "tooltip.waterworld.oxygen_tank.";
    private final OxygenTankType type;

    public OxygenTankItem(OxygenTankType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public OxygenTankType type() {
        return type;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.round(13 * OxygenTankStorage.amount(stack) / type.capacity());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x38BFE8;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(TOOLTIP + "capacity",
                Component.literal(Integer.toString(type.capacity())).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(TOOLTIP + "remaining", amountText(stack)).withStyle(ChatFormatting.GRAY));
        String rateKey = type.pressureDependent() ? "pressure_rate" : "fixed_rate";
        Component rate = Component.translatable(TOOLTIP + rateKey, format(type.perSecond())).withStyle(ChatFormatting.GREEN);
        tooltip.add(Component.translatable(TOOLTIP + "consumption", rate).withStyle(ChatFormatting.GRAY));
    }

    public static Component amountText(ItemStack stack) {
        if (stack.getItem() instanceof OxygenTankItem tank) {
            return Component.translatable(TOOLTIP + "fraction", format(OxygenTankStorage.amount(stack)),
                    tank.type.capacity()).withStyle(ChatFormatting.AQUA);
        }
        return Component.empty();
    }

    private static String format(double amount) {
        return BigDecimal.valueOf(amount).setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
