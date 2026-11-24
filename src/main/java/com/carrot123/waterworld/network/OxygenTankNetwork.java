package com.carrot123.waterworld.network;

import com.carrot123.waterworld.menu.OxygenTankMenu;
import com.carrot123.waterworld.oxygen.ChestplateOxygenInventory;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class OxygenTankNetwork {
    private static final String PROTOCOL = "1";
    private static final int OPEN_INTERVAL = 5;
    private static final Map<ServerPlayer, Integer> LAST_OPEN = Collections.synchronizedMap(new WeakHashMap<>());
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath("waterworld", "oxygen_tanks"), () -> PROTOCOL,
            PROTOCOL::equals, PROTOCOL::equals);

    private OxygenTankNetwork() {
    }

    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CHANNEL.messageBuilder(OpenRequest.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder((packet, buffer) -> {
                })
                .decoder(buffer -> new OpenRequest())
                .consumerMainThread((packet, context) -> open(context.get().getSender()))
                .add());
    }

    public static void requestOpen() {
        CHANNEL.sendToServer(new OpenRequest());
    }

    private static void open(ServerPlayer player) {
        if (player == null || !player.isAlive() || player.isSpectator() || !player.isShiftKeyDown()
                || player.isUsingItem() || player.containerMenu != player.inventoryMenu) {
            return;
        }
        ItemStack chestplate = player.getMainHandItem();
        if (!ChestplateOxygenInventory.isChestplate(chestplate)) {
            return;
        }
        int tick = player.serverLevel().getServer().getTickCount();
        Integer last = LAST_OPEN.get(player);
        if (last != null && tick - last < OPEN_INTERVAL) {
            return;
        }
        LAST_OPEN.put(player, tick);
        int slot = player.getInventory().selected;
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                (id, inventory, viewer) -> new OxygenTankMenu(id, inventory, slot, chestplate),
                Component.translatable("container.waterworld.oxygen_tanks")), data -> data.writeByte(slot));
    }

    private record OpenRequest() {
    }
}
