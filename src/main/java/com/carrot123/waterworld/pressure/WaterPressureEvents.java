package com.carrot123.waterworld.pressure;

import com.carrot123.waterworld.worldgen.OceanWorld;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "waterworld")
public final class WaterPressureEvents {
    private static final UUID SWIM_BONUS = UUID.fromString("6a237c9d-95c4-4084-95cc-82bc2bf30d24");
    private static final Map<ServerPlayer, Integer> OVERPRESSURE_TICKS = Collections.synchronizedMap(new WeakHashMap<>());

    private WaterPressureEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        DivingSuitType suit = player.isAlive() ? DivingSuitType.wornBy(player) : null;
        updateSwimming(player, suit);
        ServerLevel level = player.serverLevel();
        if (!player.isAlive() || player.isCreative() || player.isSpectator() || !player.isInWater()
                || !OceanWorld.isLayered(level)) {
            OVERPRESSURE_TICKS.remove(player);
            return;
        }

        double depth = WaterPressure.depth(level, player.getY());
        int resistance = WaterPressure.resistance(suit);
        if (depth <= resistance * 12 - 10) {
            OVERPRESSURE_TICKS.remove(player);
            return;
        }
        boolean severe = depth > resistance * 14 - 10;
        int elapsed = OVERPRESSURE_TICKS.getOrDefault(player, 0) + 1;
        if (elapsed >= (severe ? 5 : 10)) {
            OVERPRESSURE_TICKS.remove(player);
            player.hurt(WaterPressure.damageSource(level), severe ? 4.0F : 2.0F);
        } else {
            OVERPRESSURE_TICKS.put(player, elapsed);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            OVERPRESSURE_TICKS.remove(player);
            updateSwimming(player, null);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            OVERPRESSURE_TICKS.remove(player);
            updateSwimming(player, null);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        refresh(event);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        refresh(event);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        refresh(event);
    }

    private static void refresh(PlayerEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            OVERPRESSURE_TICKS.remove(player);
            updateSwimming(player, player.isAlive() ? DivingSuitType.wornBy(player) : null);
        }
    }

    private static void updateSwimming(ServerPlayer player, @Nullable DivingSuitType suit) {
        AttributeInstance speed = player.getAttribute(ForgeMod.SWIM_SPEED.get());
        if (speed == null) {
            return;
        }
        AttributeModifier previous = speed.getModifier(SWIM_BONUS);
        if (previous != null) {
            if (suit != null && previous.getAmount() == suit.swimSpeed()) {
                return;
            }
            speed.removeModifier(SWIM_BONUS);
        }
        if (suit != null) {
            speed.addTransientModifier(new AttributeModifier(SWIM_BONUS, "waterworld.diving_suit",
                    suit.swimSpeed(), AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }
}
