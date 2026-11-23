package com.carrot123.waterworld.registry;

import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModCreativeTabs {
    private static final ResourceLocation MAIN = ResourceLocation.fromNamespaceAndPath("waterworld", "main");
    private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath("waterworld", "electric_eel");

    private ModCreativeTabs() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.CREATIVE_MODE_TAB, MAIN, () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.waterworld.main"))
                .icon(() -> new ItemStack(ForgeRegistries.ITEMS.getValue(ICON)))
                .displayItems((parameters, output) -> ForgeRegistries.ITEMS.getEntries().stream()
                        .filter(entry -> entry.getKey().location().getNamespace().equals("waterworld"))
                        .filter(entry -> entry.getValue().isEnabled(parameters.enabledFeatures()))
                        .sorted(Map.Entry.comparingByKey((left, right) -> left.location().compareTo(right.location())))
                        .forEach(entry -> output.accept(entry.getValue())))
                .build());
    }
}
