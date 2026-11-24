package com.carrot123.waterworld.item.armor;

import com.carrot123.waterworld.pressure.DivingSuitType;
import com.carrot123.waterworld.menu.OxygenTankMenu;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class DivingSuitItem extends ArmorItem {
    private static final String TOOLTIP = "tooltip.waterworld.diving_suit.";
    private final DivingSuitMaterial material;

    public DivingSuitItem(DivingSuitMaterial material, Type type, Properties properties) {
        super(material, type, properties);
        this.material = material;
    }

    public DivingSuitType suit() {
        return material.suit();
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, @Nullable String type) {
        return material.texture(slot == EquipmentSlot.LEGS);
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack stack, Player player) {
        return !(player.containerMenu instanceof OxygenTankMenu menu && menu.locks(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        DivingSuitType suit = suit();
        tooltip.add(Component.translatable(TOOLTIP + "set_bonus").withStyle(ChatFormatting.AQUA));
        tooltip.add(line("max_pressure", "pressure_value", suit.resistance()));
        tooltip.add(line("max_depth", "depth_value", suit.maxDepth()));
        tooltip.add(line("swim_speed", "swim_value", Math.round(suit.swimSpeed() * 100)));
        if (getType() == Type.CHESTPLATE) {
            tooltip.add(Component.translatable("tooltip.waterworld.diving_suit.oxygen_storage").withStyle(ChatFormatting.YELLOW));
        }
    }

    private static Component line(String label, String unit, Number value) {
        Component amount = Component.translatable(TOOLTIP + unit, value).withStyle(ChatFormatting.GREEN);
        return Component.translatable(TOOLTIP + label, amount).withStyle(ChatFormatting.GRAY);
    }
}
