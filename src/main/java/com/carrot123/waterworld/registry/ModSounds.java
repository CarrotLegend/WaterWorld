package com.carrot123.waterworld.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModSounds {
    public static final RegistryObject<SoundEvent> FISHING_TREASURE = RegistryObject.create(
            ResourceLocation.fromNamespaceAndPath("waterworld", "fishing_treasure"), Registries.SOUND_EVENT, "waterworld");

    private ModSounds() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.SOUND_EVENT, FISHING_TREASURE.getId(),
                () -> SoundEvent.createVariableRangeEvent(FISHING_TREASURE.getId()));
    }
}
