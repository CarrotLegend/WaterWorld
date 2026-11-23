package com.carrot123.waterworld.blockentity;

import com.carrot123.waterworld.block.ElectricEelGeneratorBlock;
import com.carrot123.waterworld.registry.ModBlockEntities;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

public class ElectricEelGeneratorBlockEntity extends BlockEntity {
    private static final int CAPACITY = 1000;
    private static final int RATE = 20;
    private UUID pair;
    private int energy;
    private long outputTick = Long.MIN_VALUE;
    private int spent;
    private boolean retiring;
    private int capabilityGeneration;
    private LazyOptional<IEnergyStorage> readOnly = LazyOptional.of(() -> new EnergyPort(null));
    private LazyOptional<IEnergyStorage> output = LazyOptional.of(() -> new EnergyPort(outputSide()));

    public ElectricEelGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_EEL_GENERATOR.get(), pos, state);
    }

    public void formPair(UUID id) {
        pair = id;
        setChanged();
    }

    private boolean isSecondary() {
        return getBlockState().getValue(ElectricEelGeneratorBlock.SECONDARY);
    }

    private Direction outputSide() {
        return ElectricEelGeneratorBlock.outputSide(getBlockState());
    }

    private static LevelChunk loadedChunk(ServerLevel level, BlockPos pos) {
        return level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
    }

    @Nullable
    private ElectricEelGeneratorBlockEntity partner(LevelChunk chunk, BlockPos pos, BlockState ownState) {
        if (ElectricEelGeneratorBlock.isPartner(ownState, chunk.getBlockState(pos))
                && chunk.getBlockEntity(pos) instanceof ElectricEelGeneratorBlockEntity other
                && pair != null && pair.equals(other.pair) && !other.isRemoved() && !other.retiring) {
            return other;
        }
        return null;
    }

    private boolean isLive(ServerLevel server) {
        if (isRemoved() || retiring || pair == null || GeneratorRemovalData.get(server).contains(pair, worldPosition)) {
            return false;
        }
        LevelChunk ownChunk = loadedChunk(server, worldPosition);
        return ownChunk != null && ownChunk.getBlockEntity(worldPosition) == this
                && ownChunk.getBlockState(worldPosition) == getBlockState();
    }

    @Nullable
    private ElectricEelGeneratorBlockEntity core() {
        if (!(level instanceof ServerLevel server) || !isLive(server)) {
            return null;
        }
        BlockPos pos = ElectricEelGeneratorBlock.partnerPos(worldPosition, getBlockState());
        LevelChunk chunk = loadedChunk(server, pos);
        if (chunk == null) {
            return isSecondary() ? null : this;
        }
        ElectricEelGeneratorBlockEntity other = partner(chunk, pos, getBlockState());
        if (other == null || !other.isLive(server)) {
            return null;
        }
        return isSecondary() ? other : this;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ElectricEelGeneratorBlockEntity generator) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        GeneratorRemovalData removals = GeneratorRemovalData.get(server);
        if (generator.pair == null || removals.contains(generator.pair, pos)) {
            generator.removeSilently(server);
            return;
        }
        BlockPos partnerPos = ElectricEelGeneratorBlock.partnerPos(pos, state);
        LevelChunk partnerChunk = loadedChunk(server, partnerPos);
        ElectricEelGeneratorBlockEntity partner = partnerChunk == null ? null : generator.partner(partnerChunk, partnerPos, state);
        if (partnerChunk != null && (partner == null || removals.contains(generator.pair, partnerPos))) {
            generator.removeSilently(server);
            return;
        }
        if (generator.isSecondary()) {
            return;
        }
        if (generator.energy < CAPACITY) {
            generator.energy = Math.min(CAPACITY, generator.energy + RATE);
            generator.setChanged();
        }
        if (partner != null && (server.getGameTime() & 1L) != 0L) {
            generator.push(server, partner);
            generator.push(server, generator);
        } else {
            generator.push(server, generator);
            if (partner != null) {
                generator.push(server, partner);
            }
        }
    }

    public boolean mayDrop(ServerLevel server) {
        return pair != null && !retiring && !GeneratorRemovalData.get(server).contains(pair, worldPosition);
    }

    public void removePartner(ServerLevel server, BlockState oldState) {
        if (pair == null || retiring) {
            return;
        }
        GeneratorRemovalData removals = GeneratorRemovalData.get(server);
        if (removals.contains(pair, worldPosition)) {
            retiring = true;
            removals.complete(pair, worldPosition);
            return;
        }
        BlockPos pos = ElectricEelGeneratorBlock.partnerPos(worldPosition, oldState);
        LevelChunk chunk = loadedChunk(server, pos);
        if (chunk == null) {
            removals.enqueue(pair, pos);
            return;
        }
        ElectricEelGeneratorBlockEntity other = partner(chunk, pos, oldState);
        if (other != null) {
            other.removeSilently(server);
        }
    }

    private void removeSilently(ServerLevel server) {
        retiring = true;
        if (server.setBlock(worldPosition, getBlockState().getFluidState().createLegacyBlock(),
                Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS) && pair != null) {
            GeneratorRemovalData.get(server).complete(pair, worldPosition);
        }
    }

    private int available(long tick, int requested) {
        int used = outputTick == tick ? spent : 0;
        return Math.max(0, Math.min(requested, Math.min(energy, RATE - used)));
    }

    private void consume(long tick, int amount) {
        if (outputTick != tick) {
            outputTick = tick;
            spent = 0;
        }
        energy -= amount;
        spent += amount;
        setChanged();
    }

    private void push(ServerLevel server, ElectricEelGeneratorBlockEntity port) {
        long tick = server.getGameTime();
        if (available(tick, RATE) == 0 || port.core() != this) {
            return;
        }
        Direction direction = port.outputSide();
        BlockPos targetPos = port.worldPosition.relative(direction);
        LevelChunk chunk = loadedChunk(server, targetPos);
        BlockEntity target = chunk == null ? null : chunk.getBlockEntity(targetPos);
        if (target == null || target.isRemoved()) {
            return;
        }
        IEnergyStorage receiver = target.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
        if (receiver == null || !receiver.canReceive() || port.core() != this) {
            return;
        }
        int offer = available(tick, RATE);
        if (offer == 0) {
            return;
        }
        consume(tick, offer);
        int accepted = Mth.clamp(receiver.receiveEnergy(offer, false), 0, offer);
        int unused = offer - accepted;
        if (unused > 0) {
            energy += unused;
            spent -= unused;
            setChanged();
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability != ForgeCapabilities.ENERGY) {
            return super.getCapability(capability, side);
        }
        if (!(level instanceof ServerLevel server) || isRemoved() || retiring || !server.getServer().isSameThread()) {
            return LazyOptional.empty();
        }
        if (side == null) {
            return readOnly.cast();
        }
        return side == outputSide() ? output.cast() : LazyOptional.empty();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        capabilityGeneration++;
        readOnly.invalidate();
        output.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        readOnly = LazyOptional.of(() -> new EnergyPort(null));
        output = LazyOptional.of(() -> new EnergyPort(outputSide()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        pair = tag.hasUUID("Pair") ? tag.getUUID("Pair") : null;
        if (!isSecondary()) {
            energy = Mth.clamp(tag.getInt("Energy"), 0, CAPACITY);
            outputTick = tag.contains("OutputTick") ? tag.getLong("OutputTick") : Long.MIN_VALUE;
            spent = Mth.clamp(tag.getInt("OutputSpent"), 0, RATE);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (pair != null) {
            tag.putUUID("Pair", pair);
        }
        if (!isSecondary()) {
            tag.putInt("Energy", energy);
            tag.putLong("OutputTick", outputTick);
            tag.putInt("OutputSpent", spent);
        }
    }

    private class EnergyPort implements IEnergyStorage {
        private final Direction side;
        private final int generation = capabilityGeneration;

        private EnergyPort(@Nullable Direction side) {
            this.side = side;
        }

        @Nullable
        private ElectricEelGeneratorBlockEntity resolve() {
            if (generation != capabilityGeneration || !(level instanceof ServerLevel server) || !server.getServer().isSameThread()
                    || (side != null && side != outputSide())) {
                return null;
            }
            return core();
        }

        @Override
        public int receiveEnergy(int amount, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int amount, boolean simulate) {
            ElectricEelGeneratorBlockEntity core = side == null ? null : resolve();
            if (core == null) {
                return 0;
            }
            long tick = level.getGameTime();
            int extracted = core.available(tick, amount);
            if (!simulate && extracted > 0) {
                core.consume(tick, extracted);
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            ElectricEelGeneratorBlockEntity core = resolve();
            return core == null ? 0 : core.energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return resolve() == null ? 0 : CAPACITY;
        }

        @Override
        public boolean canExtract() {
            return side != null && resolve() != null;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}
