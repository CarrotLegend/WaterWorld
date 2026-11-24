package com.carrot123.waterworld.pressure;

import com.carrot123.waterworld.item.armor.DivingSuitItem;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public enum DivingSuitType {
    ATMOSPHERIC("atmospheric_diving", 36, 0.20D),
    ABYSSAL("abyssal_diving", 81, 0.40D);

    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private final String id;
    private final int resistance;
    private final double swimSpeed;

    DivingSuitType(String id, int resistance, double swimSpeed) {
        this.id = id;
        this.resistance = resistance;
        this.swimSpeed = swimSpeed;
    }

    public String id() {
        return id;
    }

    public int resistance() {
        return resistance;
    }

    public int maxDepth() {
        return WaterPressure.maxDepth(resistance);
    }

    public double swimSpeed() {
        return swimSpeed;
    }

    @Nullable
    public static DivingSuitType wornBy(Player player) {
        DivingSuitType suit = null;
        for (EquipmentSlot slot : SLOTS) {
            if (!(player.getItemBySlot(slot).getItem() instanceof DivingSuitItem piece)
                    || piece.getType().getSlot() != slot) {
                return null;
            }
            if (suit != null && suit != piece.suit()) {
                return null;
            }
            suit = piece.suit();
        }
        return suit;
    }
}
