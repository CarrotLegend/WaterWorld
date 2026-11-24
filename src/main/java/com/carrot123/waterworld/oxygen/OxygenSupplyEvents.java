package com.carrot123.waterworld.oxygen;

import com.carrot123.waterworld.item.oxygen.OxygenTankItem;
import com.carrot123.waterworld.pressure.WaterPressure;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "waterworld")
public final class OxygenSupplyEvents {
    private static final Map<ServerPlayer, Supply> LAST_SUPPLY = Collections.synchronizedMap(new WeakHashMap<>());

    private OxygenSupplyEvents() {
    }

    @SubscribeEvent
    public static void breathe(LivingBreatheEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()
                || player.isCreative() || player.isSpectator()) {
            return;
        }
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!ChestplateOxygenInventory.isChestplate(chestplate) || !ChestplateOxygenInventory.hasStoredTanks(chestplate)) {
            return;
        }
        int tick = player.serverLevel().getServer().getTickCount();
        Supply supply = LAST_SUPPLY.get(player);
        if (supply == null || supply.tick() != tick) {
            double pressure = WaterPressure.environmentalPressure(player.serverLevel(), player);
            supply = new Supply(tick, chestplate, consume(chestplate, pressure));
            LAST_SUPPLY.put(player, supply);
        }
        if (supply.chestplate() == chestplate && supply.complete() && !event.canBreathe()
                && player.isEyeInFluid(FluidTags.WATER)) {
            event.setCanBreathe(true);
            event.setCanRefillAir(true);
            event.setRefillAirAmount(Math.max(0, player.getMaxAirSupply() - player.getAirSupply()));
        }
    }

    private static boolean consume(ItemStack chestplate, double pressure) {
        NonNullList<ItemStack> tanks = ChestplateOxygenInventory.read(chestplate);
        double timeLeft = 1.0D;
        boolean changed = false;
        for (ItemStack tank : tanks) {
            if (!(tank.getItem() instanceof OxygenTankItem oxygenTank)) {
                continue;
            }
            double amount = OxygenTankStorage.amount(tank);
            if (amount <= 0.0D) {
                continue;
            }
            double rate = oxygenTank.type().perTick(pressure);
            double needed = rate * timeLeft;
            changed = true;
            if (amount >= needed) {
                OxygenTankStorage.setAmount(tank, amount - needed);
                timeLeft = 0.0D;
                break;
            }
            OxygenTankStorage.setAmount(tank, 0.0D);
            timeLeft -= amount / rate;
        }
        if (changed) {
            ChestplateOxygenInventory.write(chestplate, tanks);
        }
        return timeLeft <= 0.0D;
    }

    private record Supply(int tick, ItemStack chestplate, boolean complete) {
    }
}
