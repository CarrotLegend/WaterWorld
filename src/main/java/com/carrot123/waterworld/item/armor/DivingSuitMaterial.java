package com.carrot123.waterworld.item.armor;

import com.carrot123.waterworld.pressure.DivingSuitType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.crafting.Ingredient;

public enum DivingSuitMaterial implements ArmorMaterial {
    ATMOSPHERIC(DivingSuitType.ATMOSPHERIC, ArmorMaterials.IRON),
    ABYSSAL(DivingSuitType.ABYSSAL, ArmorMaterials.DIAMOND);

    private final DivingSuitType suit;
    private final ArmorMaterials base;

    DivingSuitMaterial(DivingSuitType suit, ArmorMaterials base) {
        this.suit = suit;
        this.base = base;
    }

    public DivingSuitType suit() {
        return suit;
    }

    public String texture(boolean leggings) {
        return "minecraft:textures/models/armor/" + base.getName() + (leggings ? "_layer_2.png" : "_layer_1.png");
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return base.getDurabilityForType(type);
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return base.getDefenseForType(type);
    }

    @Override
    public int getEnchantmentValue() {
        return base.getEnchantmentValue();
    }

    @Override
    public SoundEvent getEquipSound() {
        return base.getEquipSound();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }

    @Override
    public String getName() {
        return "waterworld:" + suit.id();
    }

    @Override
    public float getToughness() {
        return base.getToughness();
    }

    @Override
    public float getKnockbackResistance() {
        return base.getKnockbackResistance();
    }
}
