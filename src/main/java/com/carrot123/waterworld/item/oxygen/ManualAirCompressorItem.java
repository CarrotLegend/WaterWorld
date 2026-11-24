package com.carrot123.waterworld.item.oxygen;

import com.carrot123.waterworld.oxygen.OxygenTankStorage;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class ManualAirCompressorItem extends Item {
    private static final double OXYGEN_PER_TICK = 2.0D;
    private final Map<ServerPlayer, Filling> active = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<ServerPlayer, Integer> lastFillTick = Collections.synchronizedMap(new WeakHashMap<>());

    public ManualAirCompressorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack compressor = player.getItemInHand(hand);
        ItemStack tank = player.getOffhandItem();
        if (hand != InteractionHand.MAIN_HAND || !player.isAlive() || player.isSpectator()
                || player.isUsingItem() || !OxygenTankStorage.canFill(tank)) {
            return InteractionResultHolder.pass(compressor);
        }
        player.startUsingItem(hand);
        if (player instanceof ServerPlayer serverPlayer && player.isUsingItem() && player.getUseItem() == compressor) {
            active.put(serverPlayer, new Filling(compressor, tank));
        }
        return InteractionResultHolder.consume(compressor);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        Filling use = active.get(player);
        if (use == null || !player.isAlive() || player.isSpectator() || player.getUsedItemHand() != InteractionHand.MAIN_HAND
                || use.compressor() != stack || player.getMainHandItem() != stack || player.getOffhandItem() != use.tank()
                || !OxygenTankStorage.canFill(use.tank())) {
            stop(player);
            return;
        }
        int tick = player.serverLevel().getServer().getTickCount();
        if (lastFillTick.getOrDefault(player, Integer.MIN_VALUE) == tick) {
            return;
        }
        lastFillTick.put(player, tick);
        OxygenTankStorage.setAmount(use.tank(), OxygenTankStorage.amount(use.tank()) + OXYGEN_PER_TICK);
        if (!OxygenTankStorage.canFill(use.tank())) {
            stop(player);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof ServerPlayer player) {
            active.remove(player);
        }
    }

    private void stop(ServerPlayer player) {
        active.remove(player);
        player.stopUsingItem();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.waterworld.manual_air_compressor").withStyle(ChatFormatting.GRAY));
    }

    private record Filling(ItemStack compressor, ItemStack tank) {
    }
}
