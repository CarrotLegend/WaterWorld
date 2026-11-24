package com.carrot123.waterworld.oxygen;

import com.carrot123.waterworld.menu.OxygenTankMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingSwapItemsEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "waterworld")
public final class ChestplateAccessEvents {
    private ChestplateAccessEvents() {
    }

    @SubscribeEvent
    public static void attack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isOpening(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void dig(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player && isOpening(player)
                && (event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START
                || event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.STOP)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void swap(LivingSwapItemsEvent.Hands event) {
        if (event.getEntity() instanceof ServerPlayer player && player.containerMenu instanceof OxygenTankMenu menu
                && menu.locks(player.getMainHandItem())) {
            event.setCanceled(true);
        }
    }

    private static boolean isOpening(ServerPlayer player) {
        return player.isShiftKeyDown() && ChestplateOxygenInventory.isChestplate(player.getMainHandItem());
    }
}
