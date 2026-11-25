package com.carrot123.waterworld.blockentity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

final class OxygenMachineRemovalData extends SavedData {
    private final Map<UUID, Set<Long>> pending = new HashMap<>();

    static OxygenMachineRemovalData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(OxygenMachineRemovalData::load, OxygenMachineRemovalData::new,
                "waterworld_oxygen_machine_removals");
    }

    boolean contains(UUID identity, BlockPos pos) {
        return pending.getOrDefault(identity, Set.of()).contains(pos.asLong());
    }

    void enqueue(UUID identity, BlockPos pos) {
        if (pending.computeIfAbsent(identity, key -> new HashSet<>()).add(pos.asLong())) {
            setDirty();
        }
    }

    void complete(UUID identity, BlockPos pos) {
        Set<Long> positions = pending.get(identity);
        if (positions != null && positions.remove(pos.asLong())) {
            if (positions.isEmpty()) {
                pending.remove(identity);
            }
            setDirty();
        }
    }

    private static OxygenMachineRemovalData load(CompoundTag tag) {
        OxygenMachineRemovalData data = new OxygenMachineRemovalData();
        ListTag entries = tag.getList("Pending", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            if (entry.hasUUID("Structure")) {
                UUID identity = entry.getUUID("Structure");
                for (long pos : entry.getLongArray("Positions")) {
                    data.enqueue(identity, BlockPos.of(pos));
                }
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag entries = new ListTag();
        pending.forEach((identity, positions) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Structure", identity);
            entry.putLongArray("Positions", positions.stream().mapToLong(Long::longValue).toArray());
            entries.add(entry);
        });
        tag.put("Pending", entries);
        return tag;
    }
}
