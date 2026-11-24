package com.carrot123.waterworld.command;

import com.carrot123.waterworld.worldgen.OceanWorld;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "waterworld")
public final class SeaIceRepairCommand {
    private static final int MAX_COLUMNS = 4096;
    private static final String MESSAGE = "commands.waterworld.repair_sea_ice.";

    private SeaIceRepairCommand() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("waterworld").requires(source -> source.hasPermission(2))
                .then(Commands.literal("repair_sea_ice")
                        .then(Commands.argument("from", ColumnPosArgument.columnPos())
                                .then(Commands.argument("to", ColumnPosArgument.columnPos())
                                        .then(Commands.literal("preview").executes(context -> repair(context, false)))
                                        .then(Commands.literal("apply").executes(context -> repair(context, true)))))));
    }

    private static int repair(CommandContext<CommandSourceStack> context, boolean apply) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        if (!OceanWorld.isLayered(level)) {
            source.sendFailure(Component.translatable(MESSAGE + "world"));
            return 0;
        }
        ColumnPos from = ColumnPosArgument.getColumnPos(context, "from");
        ColumnPos to = ColumnPosArgument.getColumnPos(context, "to");
        int minX = Math.min(from.x(), to.x());
        int maxX = Math.max(from.x(), to.x());
        int minZ = Math.min(from.z(), to.z());
        int maxZ = Math.max(from.z(), to.z());
        int sea = OceanWorld.sea(level);
        if (!inBounds(level, minX, sea, minZ) || !inBounds(level, maxX, sea, maxZ)
                || ((long) maxX - minX + 1) * ((long) maxZ - minZ + 1) > MAX_COLUMNS) {
            source.sendFailure(Component.translatable(MESSAGE + "range", MAX_COLUMNS));
            return 0;
        }

        int modified = 0;
        int skipped = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(x >> 4, z >> 4);
                if (chunk == null) {
                    skipped++;
                    continue;
                }
                pos.set(x, sea, z);
                if (chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15) == sea
                        && chunk.getBlockState(pos).is(Blocks.ICE)
                        && chunk.getBlockState(pos.below()).is(Blocks.WATER)
                        && chunk.getFluidState(pos.below()).isSource()
                        && (!apply || level.setBlock(pos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL))) {
                    modified++;
                }
            }
        }
        int total = modified;
        int unavailable = skipped;
        source.sendSuccess(() -> Component.translatable(MESSAGE + (apply ? "applied" : "preview"), total, unavailable), apply);
        return total;
    }

    private static boolean inBounds(ServerLevel level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return level.isInWorldBounds(pos) && level.getWorldBorder().isWithinBounds(pos);
    }
}
