package com.carrot123.waterworld.blockentity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

final class RecyclingStationRemovalData extends SavedData {
    private final Map<UUID, BlockPos> pending = new HashMap<>();

    static RecyclingStationRemovalData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(RecyclingStationRemovalData::load, RecyclingStationRemovalData::new,
                "waterworld_recycling_station_removals");
    }

    private static RecyclingStationRemovalData load(CompoundTag tag) {
        RecyclingStationRemovalData data = new RecyclingStationRemovalData();
        ListTag entries = tag.getList("Pending", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            if (entry.hasUUID("Pair")) {
                data.pending.put(entry.getUUID("Pair"), BlockPos.of(entry.getLong("Pos")));
            }
        }
        return data;
    }

    boolean contains(UUID pair, BlockPos pos) {
        return pos.equals(pending.get(pair));
    }

    void enqueue(UUID pair, BlockPos pos) {
        pending.put(pair, pos.immutable());
        setDirty();
    }

    void complete(UUID pair, BlockPos pos) {
        if (pending.remove(pair, pos)) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag entries = new ListTag();
        pending.forEach((pair, pos) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Pair", pair);
            entry.putLong("Pos", pos.asLong());
            entries.add(entry);
        });
        tag.put("Pending", entries);
        return tag;
    }
}
