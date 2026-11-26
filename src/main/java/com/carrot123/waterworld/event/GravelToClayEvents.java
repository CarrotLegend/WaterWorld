package com.carrot123.waterworld.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GravelToClayEvents {
    private GravelToClayEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        ItemStack stack = event.getItemStack();
        if (player.isSpectator() || event.getUseBlock() == Event.Result.DENY
                || event.getUseItem() == Event.Result.DENY || !isWaterBottle(stack)
                || !level.getBlockState(pos).is(Blocks.GRAVEL)) {
            return;
        }
        if (level.isClientSide) {
            finishInteraction(event, InteractionResult.SUCCESS);
            return;
        }
        if (!(level instanceof ServerLevel server) || !server.mayInteract(player, pos)
                || (!player.getAbilities().mayBuild && !stack.hasAdventureModePlaceTagForBlock(
                        server.registryAccess().registryOrThrow(Registries.BLOCK), new BlockInWorld(server, pos, false)))) {
            return;
        }
        if (replaceGravel(server, event, stack)) {
            finishInteraction(event, InteractionResult.CONSUME);
        }
    }

    private static boolean replaceGravel(ServerLevel level, PlayerInteractEvent.RightClickBlock event, ItemStack stack) {
        if (level.captureBlockSnapshots || level.restoringBlockSnapshots) {
            return false;
        }
        BlockPos pos = event.getPos();
        BlockState clay = Blocks.CLAY.defaultBlockState();
        int snapshotIndex = level.capturedBlockSnapshots.size();
        level.captureBlockSnapshots = true;
        try {
            if (!level.setBlock(pos, clay, Block.UPDATE_ALL)) {
                return false;
            }
        } finally {
            level.captureBlockSnapshots = false;
        }

        BlockSnapshot snapshot = level.capturedBlockSnapshots.remove(snapshotIndex);
        Player player = event.getEntity();
        boolean accepted = false;
        try {
            accepted = !ForgeEventFactory.onBlockPlace(player, snapshot, event.getHitVec().getDirection())
                    && level.getBlockState(pos) == clay && event.getItemStack() == stack && isWaterBottle(stack);
        } finally {
            if (!accepted) {
                level.restoringBlockSnapshots = true;
                try {
                    snapshot.restore(true, false);
                } finally {
                    level.restoringBlockSnapshots = false;
                }
            }
        }
        if (!accepted) {
            return false;
        }

        if (!player.getAbilities().instabuild) {
            player.setItemInHand(event.getHand(), ItemUtils.createFilledResult(
                    stack, player, new ItemStack(Items.GLASS_BOTTLE), false));
        }
        BlockState gravel = snapshot.getReplacedBlock();
        clay.onPlace(level, pos, gravel, false);
        level.markAndNotifyBlock(pos, level.getChunkAt(pos), gravel, clay, snapshot.getFlag(), 512);
        return true;
    }

    private static boolean isWaterBottle(ItemStack stack) {
        return stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER;
    }

    private static void finishInteraction(PlayerInteractEvent.RightClickBlock event, InteractionResult result) {
        event.setCancellationResult(result);
        event.setCanceled(true);
    }
}
