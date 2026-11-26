package com.carrot123.waterworld.blockentity;

import com.carrot123.waterworld.block.FishingBoxBlock;
import com.carrot123.waterworld.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class FishingBoxBlockEntity extends BaseContainerBlockEntity {
    private static final int FISHING_BOX_RANGE = 8;
    private final NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private final List<ItemStack> pending = new ArrayList<>();
    private int fishingTicks;

    public FishingBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISHING_BOX.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FishingBoxBlockEntity box) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!box.pending.isEmpty()) {
            box.storePending();
            return;
        }
        if (!state.getValue(BlockStateProperties.WATERLOGGED) || !box.hasSpace()
                || !hasClearFishingArea(serverLevel, pos)) {
            return;
        }
        box.fishingTicks++;
        box.setChanged();
        if (box.fishingTicks < 200) {
            return;
        }
        box.fishingTicks = 0;
        box.catchLoot(serverLevel, pos, ((FishingBoxBlock) state.getBlock()).isAdvanced());
        box.storePending();
    }

    private static boolean hasClearFishingArea(ServerLevel level, BlockPos pos) {
        int minX = pos.getX() - FISHING_BOX_RANGE;
        int maxX = pos.getX() + FISHING_BOX_RANGE;
        int minY = pos.getY() - FISHING_BOX_RANGE;
        int maxY = pos.getY() + FISHING_BOX_RANGE;
        int minZ = pos.getZ() - FISHING_BOX_RANGE;
        int maxZ = pos.getZ() + FISHING_BOX_RANGE;

        for (int chunkX = minX >> 4; chunkX <= (maxX >> 4); chunkX++) {
            for (int chunkZ = minZ >> 4; chunkZ <= (maxZ >> 4); chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    return false;
                }
                for (BlockPos candidate : chunk.getBlockEntitiesPos()) {
                    if (!pos.equals(candidate)
                            && candidate.getX() >= minX && candidate.getX() <= maxX
                            && candidate.getY() >= minY && candidate.getY() <= maxY
                            && candidate.getZ() >= minZ && candidate.getZ() <= maxZ
                            && chunk.getBlockState(candidate).getBlock() instanceof FishingBoxBlock) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void catchLoot(ServerLevel level, BlockPos pos, boolean advanced) {
        ItemStack rod = new ItemStack(Items.FISHING_ROD);
        if (advanced) {
            rod.enchant(Enchantments.FISHING_LUCK, 5);
        }
        FishingHook hook = new FishingHook(EntityType.FISHING_BOBBER, level);
        hook.setPos(Vec3.atCenterOf(pos));
        try {
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, hook.position())
                    .withParameter(LootContextParams.TOOL, rod)
                    .withParameter(LootContextParams.THIS_ENTITY, hook)
                    .withLuck(advanced ? 5.0F : 0.0F)
                    .create(LootContextParamSets.FISHING);
            LootTable table = level.getServer().getLootData().getLootTable(BuiltInLootTables.FISHING);
            for (ItemStack stack : table.getRandomItems(params)) {
                if (!stack.isEmpty()) {
                    pending.add(stack.copy());
                }
            }
            setChanged();
        } finally {
            hook.discard();
        }
    }

    private boolean hasSpace() {
        for (ItemStack stack : items) {
            if (stack.isEmpty() || stack.getCount() < Math.min(getMaxStackSize(), stack.getMaxStackSize())) {
                return true;
            }
        }
        return false;
    }

    private void storePending() {
        boolean changed = false;
        Iterator<ItemStack> iterator = pending.iterator();
        while (iterator.hasNext()) {
            ItemStack reward = iterator.next();
            int previous = reward.getCount();
            for (ItemStack stored : items) {
                if (ItemStack.isSameItemSameTags(stored, reward)) {
                    int count = Math.min(reward.getCount(), Math.max(0,
                            Math.min(getMaxStackSize(), stored.getMaxStackSize()) - stored.getCount()));
                    stored.grow(count);
                    reward.shrink(count);
                }
                if (reward.isEmpty()) {
                    break;
                }
            }
            for (int slot = 0; slot < items.size() && !reward.isEmpty(); slot++) {
                if (items.get(slot).isEmpty()) {
                    items.set(slot, reward.split(Math.min(getMaxStackSize(), reward.getMaxStackSize())));
                }
            }
            changed |= reward.getCount() != previous;
            if (reward.isEmpty()) {
                iterator.remove();
                changed = true;
            }
        }
        if (changed) {
            setChanged();
        }
    }

    public void dropPending(Level level, BlockPos pos) {
        for (ItemStack stack : pending) {
            Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
        }
        pending.clear();
        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        fishingTicks = Math.max(0, Math.min(199, tag.getInt("FishingTicks")));
        pending.clear();
        ListTag rewards = tag.getList("PendingRewards", Tag.TAG_COMPOUND);
        for (int index = 0; index < rewards.size(); index++) {
            ItemStack stack = ItemStack.of(rewards.getCompound(index));
            if (!stack.isEmpty()) {
                pending.add(stack);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("FishingTicks", fishingTicks);
        ListTag rewards = new ListTag();
        for (ItemStack stack : pending) {
            rewards.add(stack.save(new CompoundTag()));
        }
        tag.put("PendingRewards", rewards);
    }

    @Override
    protected Component getDefaultName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, count);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.setCount(Math.min(stack.getCount(), Math.min(getMaxStackSize(), stack.getMaxStackSize())));
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }
}
