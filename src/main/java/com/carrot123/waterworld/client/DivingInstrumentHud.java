package com.carrot123.waterworld.client;

import com.carrot123.waterworld.network.DivingHudNetwork;
import com.carrot123.waterworld.network.DivingHudNetwork.Mode;
import com.carrot123.waterworld.network.DivingHudNetwork.Snapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DivingInstrumentHud {
    private static final String TEXT = "hud.waterworld.diving.";
    @Nullable private static Snapshot latest;

    private DivingInstrumentHud() {
    }

    @SubscribeEvent
    public static void register(RegisterGuiOverlaysEvent event) {
        DivingHudNetwork.setClientHandler(packet -> latest = packet.mode() == Mode.OFF ? null : packet);
        event.registerAboveAll("diving_instrument", DivingInstrumentHud::render);
    }

    private static boolean valid(@Nullable LocalPlayer player, Snapshot snapshot) {
        return player != null && player.isAlive() && player.getId() == snapshot.playerId()
                && player.level().dimension().location().equals(snapshot.dimension());
    }

    private static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        Snapshot snapshot = latest;
        if (snapshot == null || minecraft.options.hideGui || !valid(minecraft.player, snapshot)) {
            return;
        }
        Font font = minecraft.font;
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(TEXT + "depth", decimal(snapshot.depth())));
        lines.add(Component.translatable(TEXT + "pressure", decimal(snapshot.pressure())));
        boolean computer = snapshot.mode() == Mode.COMPUTER;
        if (computer) {
            String oxygen = snapshot.capacity() == 0 ? "0" : decimal(snapshot.oxygen());
            String rate = snapshot.rate() == 0.0D ? "0" : decimal(snapshot.rate());
            lines.add(Component.translatable(TEXT + "oxygen", oxygen, snapshot.capacity()));
            lines.add(Component.translatable(TEXT + (snapshot.paused() ? "paused" : "rate"), rate));
            lines.add(Component.translatable(TEXT + "time", time(snapshot.timeSeconds())));
        }
        int width = lines.stream().mapToInt(font::width).max().orElse(0) + 14;
        int height = 11 * lines.size() + (computer ? 16 : 10);
        float scale = Math.min(1.0F, Math.min((screenWidth - 12.0F) / width, (screenHeight - 74.0F) / height));
        if (scale <= 0.0F) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(screenWidth - 6 - width * scale, 8, 0);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.fill(0, 0, width, height, 0xe6152d44);
        graphics.fill(0, 0, 2, height, 0xff58bde7);
        int y = 5;
        for (Component line : lines) {
            graphics.drawString(font, line, 7, y, 0xffecf8ff, false);
            y += 11;
        }
        if (computer) {
            double fraction = snapshot.capacity() == 0 ? 0.0D : snapshot.oxygen() / snapshot.capacity();
            int barWidth = width - 14;
            graphics.fill(7, y + 2, 7 + barWidth, y + 6, 0xff365067);
            int color = fraction < 0.2D ? 0xffffbd66 : 0xff56c8ed;
            graphics.fill(7, y + 2, 7 + (int) (fraction * barWidth), y + 6, color);
        }
        graphics.pose().popPose();
    }

    private static String decimal(double number) {
        return String.format(Locale.ROOT, "%.1f", number);
    }

    private static String time(int seconds) {
        return seconds >= 3600 ? String.format(Locale.ROOT, "%d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60)
                : String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60);
    }

    @Mod.EventBusSubscriber(modid = "waterworld", value = Dist.CLIENT)
    public static final class Lifecycle {
        private Lifecycle() {
        }

        @SubscribeEvent
        public static void tick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END && latest != null && !valid(Minecraft.getInstance().player, latest)) {
                latest = null;
            }
        }

        @SubscribeEvent
        public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
            latest = null;
        }
    }
}
