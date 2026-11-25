package com.carrot123.waterworld.registry;

import com.carrot123.waterworld.item.ElectricEelGeneratorItem;
import com.carrot123.waterworld.item.DivingInstrumentItem;
import com.carrot123.waterworld.item.OxygenMachineItem;
import com.carrot123.waterworld.item.RecyclingStationItem;
import com.carrot123.waterworld.item.SeawaterFilterItem;
import com.carrot123.waterworld.item.armor.DivingSuitItem;
import com.carrot123.waterworld.item.armor.DivingSuitMaterial;
import com.carrot123.waterworld.item.oxygen.ManualAirCompressorItem;
import com.carrot123.waterworld.item.oxygen.OxygenTankItem;
import com.carrot123.waterworld.item.oxygen.OxygenTankType;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
    public static final RegistryObject<Item> SCRAP_IRON = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "scrap_iron"), Registries.ITEM, "waterworld");
    public static final RegistryObject<Item> SCREW = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "screw"), Registries.ITEM, "waterworld");
    public static final RegistryObject<Item> RECYCLING_HAMMER = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "recycling_hammer"), Registries.ITEM, "waterworld");
    public static final RegistryObject<Item> PRESSURE_GAUGE = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "pressure_gauge"), Registries.ITEM, "waterworld");
    public static final RegistryObject<Item> DIVING_COMPUTER = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "diving_computer"), Registries.ITEM, "waterworld");

    @SubscribeEvent
    public static void registerItems(RegisterEvent event) {
        event.register(Registries.ITEM, ModBlocks.ELECTRIC_AIR_COMPRESSOR.getId(),
                () -> new OxygenMachineItem(ModBlocks.ELECTRIC_AIR_COMPRESSOR.get(), new Item.Properties()));
        event.register(Registries.ITEM, ModBlocks.ELECTROLYTIC_OXYGEN_GENERATOR.getId(),
                () -> new OxygenMachineItem(ModBlocks.ELECTROLYTIC_OXYGEN_GENERATOR.get(), new Item.Properties()));
        event.register(Registries.ITEM, PRESSURE_GAUGE.getId(), () -> new DivingInstrumentItem(new Item.Properties().stacksTo(1)));
        event.register(Registries.ITEM, DIVING_COMPUTER.getId(), () -> new DivingInstrumentItem(new Item.Properties().stacksTo(1)));
        for (OxygenTankType type : OxygenTankType.values()) {
            event.register(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("waterworld", type.id()),
                    () -> new OxygenTankItem(type, new Item.Properties().stacksTo(1)));
        }
        event.register(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("waterworld", "manual_air_compressor"),
                () -> new ManualAirCompressorItem(new Item.Properties().stacksTo(1)));
        for (DivingSuitMaterial material : DivingSuitMaterial.values()) {
            for (ArmorItem.Type part : ArmorItem.Type.values()) {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath("waterworld", material.suit().id() + "_" + part.getName());
                event.register(Registries.ITEM, id, () -> new DivingSuitItem(material, part, new Item.Properties().stacksTo(1)));
            }
        }
        event.register(Registries.ITEM, ModBlocks.SEAWATER_FILTER.getId(),
                () -> new SeawaterFilterItem(ModBlocks.SEAWATER_FILTER.get(), new Item.Properties()));
        event.register(Registries.ITEM, ModBlocks.ELECTRIC_EEL_GENERATOR.getId(),
                () -> new ElectricEelGeneratorItem(ModBlocks.ELECTRIC_EEL_GENERATOR.get(), new Item.Properties()));
        event.register(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("waterworld", "fishing_box"),
                () -> new BlockItem(ModBlocks.FISHING_BOX.get(), new Item.Properties()));
        event.register(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("waterworld", "advanced_fishing_box"),
                () -> new BlockItem(ModBlocks.ADVANCED_FISHING_BOX.get(), new Item.Properties()));
        event.register(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("waterworld", "electric_eel"),
                () -> new Item(new Item.Properties().stacksTo(64).food(Foods.TROPICAL_FISH)));
        event.register(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("waterworld", "plastic"),
                () -> new Item(new Item.Properties().stacksTo(64)));
        for (String material : List.of("rubber", "raw_titanium", "titanium_ingot", "raw_aluminum", "aluminum_ingot",
                "titanium_alloy_ingot", "synthetic_fiber", "carbon_fiber", "circuit_board", "advanced_circuit_board")) {
            event.register(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("waterworld", material),
                    () -> new Item(new Item.Properties().stacksTo(64)));
        }
        event.register(Registries.ITEM, SCREW.getId(),
                () -> new Item(new Item.Properties().stacksTo(64)));
        event.register(Registries.ITEM, SCRAP_IRON.getId(),
                () -> new Item(new Item.Properties().stacksTo(64)));
        event.register(Registries.ITEM, RECYCLING_HAMMER.getId(),
                () -> new Item(new Item.Properties().stacksTo(1)));
        event.register(Registries.ITEM, ModBlocks.RECYCLING_STATION.getId(),
                () -> new RecyclingStationItem(ModBlocks.RECYCLING_STATION.get(), new Item.Properties()));
    }
}
