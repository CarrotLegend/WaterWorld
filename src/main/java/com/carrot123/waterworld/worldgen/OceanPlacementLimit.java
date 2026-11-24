package com.carrot123.waterworld.worldgen;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "waterworld")
public final class OceanPlacementLimit {
    public static final int MAX_PLACEMENT_Y = 1048;

    private OceanPlacementLimit() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getLevel() instanceof ServerLevel level)
                || !OceanWorld.isLayered(level)) {
            return;
        }
        if (event instanceof BlockEvent.EntityMultiPlaceEvent multiple) {
            if (multiple.getReplacedBlockSnapshots().stream().anyMatch(snapshot -> snapshot.getPos().getY() > MAX_PLACEMENT_Y)) {
                event.setCanceled(true);
            }
        } else if (event.getPos().getY() > MAX_PLACEMENT_Y) {
            event.setCanceled(true);
        }
    }
}
