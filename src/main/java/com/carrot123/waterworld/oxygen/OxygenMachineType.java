package com.carrot123.waterworld.oxygen;

import com.carrot123.waterworld.blockentity.OxygenMachineBlockEntity;
import com.carrot123.waterworld.menu.OxygenMachineMenu;
import com.carrot123.waterworld.registry.ModBlockEntities;
import com.carrot123.waterworld.registry.ModMenus;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public enum OxygenMachineType {
    COMPRESSOR("electric_air_compressor", 1, 2, 10000, 15, 0, 0),
    ELECTROLYZER("electrolytic_oxygen_generator", 2, 6, 40000, 60, 4000, 3);

    private final String id;
    private final int height;
    private final int oxygenPerTick;
    private final int energyCapacity;
    private final int energyPerTick;
    private final int waterCapacity;
    private final int waterPerTick;

    OxygenMachineType(String id, int height, int oxygenPerTick, int energyCapacity, int energyPerTick,
                       int waterCapacity, int waterPerTick) {
        this.id = id;
        this.height = height;
        this.oxygenPerTick = oxygenPerTick;
        this.energyCapacity = energyCapacity;
        this.energyPerTick = energyPerTick;
        this.waterCapacity = waterCapacity;
        this.waterPerTick = waterPerTick;
    }

    public String id() {
        return id;
    }

    public int size() {
        return 2 * height;
    }

    public int oxygenPerTick() {
        return oxygenPerTick;
    }

    public int energyCapacity() {
        return energyCapacity;
    }

    public int energyPerTick() {
        return energyPerTick;
    }

    public int waterCapacity() {
        return waterCapacity;
    }

    public int waterPerTick() {
        return waterPerTick;
    }

    public boolean usesWater() {
        return waterCapacity > 0;
    }

    public BlockEntityType<OxygenMachineBlockEntity> blockEntityType() {
        return usesWater() ? ModBlockEntities.ELECTROLYTIC_OXYGEN_GENERATOR.get() : ModBlockEntities.ELECTRIC_AIR_COMPRESSOR.get();
    }

    public MenuType<OxygenMachineMenu> menuType() {
        return usesWater() ? ModMenus.ELECTROLYTIC_OXYGEN_GENERATOR.get() : ModMenus.ELECTRIC_AIR_COMPRESSOR.get();
    }
}
