package com.carrot123.waterworld.client;

import com.carrot123.waterworld.network.OxygenTankNetwork;
import com.carrot123.waterworld.oxygen.ChestplateOxygenInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "waterworld", value = Dist.CLIENT)
public final class OxygenTankInput {
    private static boolean requested;

    private OxygenTankInput() {
    }

    @SubscribeEvent
    public static void attack(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.screen != null || minecraft.gameMode == null || player.isSpectator()
                || !player.isAlive() || !player.isShiftKeyDown()
                || !ChestplateOxygenInventory.isChestplate(player.getMainHandItem())) {
            return;
        }
        event.setSwingHand(false);
        event.setCanceled(true);
        if (minecraft.gameMode.isDestroying()) {
            minecraft.gameMode.stopDestroyBlock();
        }
        if (!requested) {
            requested = true;
            OxygenTankNetwork.requestOpen();
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !Minecraft.getInstance().options.keyAttack.isDown()) {
            requested = false;
        }
    }
}
