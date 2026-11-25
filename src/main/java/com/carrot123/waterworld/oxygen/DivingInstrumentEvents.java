package com.carrot123.waterworld.oxygen;

import com.carrot123.waterworld.item.oxygen.OxygenTankItem;
import com.carrot123.waterworld.network.DivingHudNetwork;
import com.carrot123.waterworld.network.DivingHudNetwork.Mode;
import com.carrot123.waterworld.network.DivingHudNetwork.Snapshot;
import com.carrot123.waterworld.pressure.WaterPressure;
import com.carrot123.waterworld.registry.ModItems;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = "waterworld")
public final class DivingInstrumentEvents {
    private static final int UPDATE_INTERVAL = 5;
    private static final Map<ServerPlayer, LastUpdate> LAST_UPDATE = Collections.synchronizedMap(new WeakHashMap<>());

    private DivingInstrumentEvents() {
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        Mode mode = player.isAlive() ? wornInstrument(player) : Mode.OFF;
        LastUpdate previous = LAST_UPDATE.get(player);
        if (mode == Mode.OFF) {
            clear(player);
            return;
        }
        boolean paused = player.isCreative() || player.isSpectator();
        int tick = player.serverLevel().getServer().getTickCount();
        if (previous == null || previous.mode() != mode || previous.paused() != paused
                || tick - previous.tick() >= UPDATE_INTERVAL) {
            LAST_UPDATE.put(player, new LastUpdate(mode, paused, tick));
            DivingHudNetwork.send(player, snapshot(player, mode, paused));
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR)
    public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clear(player);
        }
    }

    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clear(player);
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LAST_UPDATE.remove(player);
        }
    }

    private static void clear(ServerPlayer player) {
        if (LAST_UPDATE.remove(player) != null) {
            DivingHudNetwork.send(player, snapshot(player, Mode.OFF, false));
        }
    }

    private static Mode wornInstrument(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player).map(inventory -> {
            var hands = inventory.getCurios().get("hands");
            if (hands == null) {
                return Mode.OFF;
            }
            var slots = hands.getStacks();
            Mode worn = Mode.OFF;
            for (int index = 0; index < slots.getSlots(); index++) {
                ItemStack stack = slots.getStackInSlot(index);
                if (stack.is(ModItems.DIVING_COMPUTER.get())) {
                    return Mode.COMPUTER;
                }
                if (stack.is(ModItems.PRESSURE_GAUGE.get())) {
                    worn = Mode.GAUGE;
                }
            }
            return worn;
        }).orElse(Mode.OFF);
    }

    private static Snapshot snapshot(ServerPlayer player, Mode mode, boolean paused) {
        double depth = mode == Mode.OFF ? 0.0D : WaterPressure.environmentalDepth(player.serverLevel(), player);
        double pressure = WaterPressure.pressure(depth);
        double oxygen = 0.0D;
        double rate = 0.0D;
        double timeSeconds = 0.0D;
        int capacity = 0;
        if (mode == Mode.COMPUTER) {
            for (ItemStack tank : ChestplateOxygenInventory.read(player.getItemBySlot(EquipmentSlot.CHEST))) {
                if (tank.getItem() instanceof OxygenTankItem oxygenTank) {
                    double amount = OxygenTankStorage.amount(tank);
                    double tankRate = oxygenTank.type().perTick(pressure) * 20.0D;
                    capacity += oxygenTank.type().capacity();
                    oxygen += amount;
                    timeSeconds += amount / tankRate;
                    if (!paused && rate == 0.0D && amount > 0.0D) {
                        rate = tankRate;
                    }
                }
            }
        }
        return new Snapshot(player.getId(), player.serverLevel().dimension().location(), mode, depth, pressure,
                oxygen, capacity, rate, (int) timeSeconds, paused);
    }

    private record LastUpdate(Mode mode, boolean paused, int tick) {
    }
}
