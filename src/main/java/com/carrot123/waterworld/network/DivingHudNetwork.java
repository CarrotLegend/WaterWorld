package com.carrot123.waterworld.network;

import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DivingHudNetwork {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath("waterworld", "diving_hud"), () -> PROTOCOL,
            PROTOCOL::equals, PROTOCOL::equals);
    private static Consumer<Snapshot> clientHandler = snapshot -> {};

    private DivingHudNetwork() {
    }

    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CHANNEL.messageBuilder(Snapshot.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Snapshot::write)
                .decoder(Snapshot::read)
                .consumerMainThread((packet, context) -> clientHandler.accept(packet))
                .add());
    }

    public static void setClientHandler(Consumer<Snapshot> handler) {
        clientHandler = handler;
    }

    public static void send(ServerPlayer player, Snapshot snapshot) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), snapshot);
    }

    public enum Mode {
        OFF, GAUGE, COMPUTER
    }

    public record Snapshot(int playerId, ResourceLocation dimension, Mode mode, double depth, double pressure,
                           double oxygen, int capacity, double rate, int timeSeconds, boolean paused) {
        private void write(FriendlyByteBuf buffer) {
            buffer.writeVarInt(playerId);
            buffer.writeResourceLocation(dimension);
            buffer.writeEnum(mode);
            if (mode != Mode.OFF) {
                buffer.writeDouble(depth);
                buffer.writeDouble(pressure);
            }
            if (mode == Mode.COMPUTER) {
                buffer.writeDouble(oxygen);
                buffer.writeVarInt(capacity);
                buffer.writeDouble(rate);
                buffer.writeVarInt(timeSeconds);
                buffer.writeBoolean(paused);
            }
        }

        private static Snapshot read(FriendlyByteBuf buffer) {
            int id = buffer.readVarInt();
            ResourceLocation dimension = buffer.readResourceLocation();
            Mode mode = buffer.readEnum(Mode.class);
            double depth = mode == Mode.OFF ? 0.0D : buffer.readDouble();
            double pressure = mode == Mode.OFF ? 1.0D : buffer.readDouble();
            if (mode != Mode.COMPUTER) {
                return new Snapshot(id, dimension, mode, depth, pressure, 0.0D, 0, 0.0D, 0, false);
            }
            return new Snapshot(id, dimension, mode, depth, pressure, buffer.readDouble(), buffer.readVarInt(),
                    buffer.readDouble(), buffer.readVarInt(), buffer.readBoolean());
        }
    }
}
