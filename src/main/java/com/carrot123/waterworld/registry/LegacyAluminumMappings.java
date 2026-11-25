package com.carrot123.waterworld.registry;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LegacyAluminumMappings {
    private static final Logger LOGGER = LogUtils.getLogger();

    private LegacyAluminumMappings() {
    }

    @SubscribeEvent
    public static void remapItems(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<Item> mapping : event.getMappings(Registries.ITEM, "waterworld")) {
            String targetPath = switch (mapping.getKey().getPath()) {
                case "raw_aluminum" -> "raw_aluminum";
                case "aluminum_ingot" -> "ingot_aluminum";
                default -> null;
            };
            if (targetPath == null) {
                continue;
            }
            ResourceLocation targetId = ResourceLocation.fromNamespaceAndPath("immersiveengineering", targetPath);
            Item target = ForgeRegistries.ITEMS.containsKey(targetId) ? ForgeRegistries.ITEMS.getValue(targetId) : null;
            if (target != null) {
                mapping.remap(target);
            } else {
                LOGGER.error("Cannot migrate legacy WaterWorld item {}: {} is missing. "
                        + "Install a compatible version of Immersive Engineering before opening this world.",
                        mapping.getKey(), targetId);
                mapping.fail();
            }
        }
    }
}
