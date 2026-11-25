package com.carrot123.waterworld.blockentity;

import com.carrot123.waterworld.block.OxygenMachineBlock;
import com.carrot123.waterworld.item.oxygen.OxygenTankItem;
import com.carrot123.waterworld.menu.OxygenMachineMenu;
import com.carrot123.waterworld.oxygen.OxygenMachineType;
import com.carrot123.waterworld.oxygen.OxygenTankStorage;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public final class OxygenMachineBlockEntity extends BlockEntity {
    public static final int IDLE = 0;
    public static final int WORKING = 1;
    public static final int NEEDS_ENERGY = 2;
    public static final int NEEDS_WATER = 3;
    private final OxygenMachineType type;
    @Nullable private final SimpleContainer tanks;
    @Nullable private final FluidTank water;
    @Nullable private UUID identity;
    private int energy;
    private int workStatus = IDLE;
    private boolean retiring;
    private boolean removalOrigin;
    private int capabilityGeneration;
    private LazyOptional<IEnergyStorage> energyInput = LazyOptional.of(() -> new EnergyPort(true));
    private LazyOptional<IEnergyStorage> energyView = LazyOptional.of(() -> new EnergyPort(false));
    private LazyOptional<IFluidHandler> waterInput = LazyOptional.of(() -> new WaterPort(true));
    private LazyOptional<IFluidHandler> waterView = LazyOptional.of(() -> new WaterPort(false));

    public OxygenMachineBlockEntity(BlockPos pos, BlockState state) {
        super(((OxygenMachineBlock) state.getBlock()).machineType().blockEntityType(), pos, state);
        type = ((OxygenMachineBlock) state.getBlock()).machineType();
        tanks = state.getValue(OxygenMachineBlock.PART) == 0 ? new SimpleContainer(type.size()) {
            @Override
            public void setChanged() {
                super.setChanged();
                OxygenMachineBlockEntity.this.setChanged();
            }
        } : null;
        water = tanks != null && type.usesWater() ? new FluidTank(type.waterCapacity(),
                fluid -> fluid.getFluid() == Fluids.WATER) : null;
    }

    public void form(UUID structureId) {
        identity = structureId;
        setChanged();
    }

    private static LevelChunk loadedChunk(ServerLevel level, BlockPos pos) {
        return level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
    }

    @Nullable
    private OxygenMachineBlockEntity match(LevelChunk chunk, BlockPos pos, BlockState expected) {
        if (chunk.getBlockState(pos) == expected
                && chunk.getBlockEntity(pos) instanceof OxygenMachineBlockEntity member
                && identity != null && identity.equals(member.identity) && !member.retiring && !member.isRemoved()) {
            return member;
        }
        return null;
    }

    private boolean hasBrokenMember(ServerLevel server, BlockState state) {
        BlockPos root = OxygenMachineBlock.corePosition(worldPosition, state);
        for (int part = 0; part < type.size(); part++) {
            BlockPos pos = OxygenMachineBlock.position(root, state, part);
            LevelChunk chunk = loadedChunk(server, pos);
            if (chunk != null && match(chunk, pos, state.setValue(OxygenMachineBlock.PART, part)) == null) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public OxygenMachineBlockEntity core() {
        if (!(level instanceof ServerLevel server) || !server.getServer().isSameThread()
                || retiring || isRemoved() || identity == null) {
            return null;
        }
        BlockState state = getBlockState();
        BlockPos root = OxygenMachineBlock.corePosition(worldPosition, state);
        OxygenMachineBlockEntity core = null;
        OxygenMachineRemovalData removals = OxygenMachineRemovalData.get(server);
        for (int part = 0; part < type.size(); part++) {
            BlockPos pos = OxygenMachineBlock.position(root, state, part);
            LevelChunk chunk = loadedChunk(server, pos);
            if (chunk == null || removals.contains(identity, pos)) {
                return null;
            }
            OxygenMachineBlockEntity member = match(chunk, pos, state.setValue(OxygenMachineBlock.PART, part));
            if (member == null || (pos.equals(worldPosition) && member != this)) {
                return null;
            }
            if (part == 0) {
                core = member;
            }
        }
        return core != null && core.tanks != null ? core : null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OxygenMachineBlockEntity member) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        OxygenMachineRemovalData removals = OxygenMachineRemovalData.get(server);
        if (member.identity == null || member.retiring || removals.contains(member.identity, pos)
                || state.getValue(OxygenMachineBlock.PART) >= member.type.size()) {
            member.removePart(server, pos);
            return;
        }
        if (member.tanks != null && member.core() == member) {
            member.chargeTanks();
        } else if (member.hasBrokenMember(server, state)) {
            member.removePart(server, pos);
        }
    }

    private void chargeTanks() {
        workStatus = IDLE;
        double budget = type.oxygenPerTick();
        for (int slot = 0; slot < tanks.getContainerSize(); slot++) {
            ItemStack stack = tanks.getItem(slot);
            if (!OxygenTankStorage.canFill(stack)) {
                continue;
            }
            if (workStatus != WORKING) {
                if (energy < type.energyPerTick()) {
                    workStatus = NEEDS_ENERGY;
                    return;
                }
                if (water != null && water.getFluidAmount() < type.waterPerTick()) {
                    workStatus = NEEDS_WATER;
                    return;
                }
                energy -= type.energyPerTick();
                if (water != null) {
                    water.drain(type.waterPerTick(), IFluidHandler.FluidAction.EXECUTE);
                }
                workStatus = WORKING;
            }
            OxygenTankItem tank = (OxygenTankItem) stack.getItem();
            double amount = OxygenTankStorage.amount(stack);
            double difference = tank.type().capacity() - amount;
            double filled = Math.min(budget, difference);
            OxygenTankStorage.setAmount(stack, filled == difference ? tank.type().capacity() : amount + filled);
            budget -= filled;
            if (budget <= 0.0D) {
                break;
            }
        }
        if (workStatus == WORKING) {
            tanks.setChanged();
        }
    }

    public MenuProvider menuProvider(OxygenMachineBlockEntity openedPart) {
        return new SimpleMenuProvider((id, inventory, viewer) -> new OxygenMachineMenu(id, inventory, type,
                tanks, this, openedPart), Component.translatable("block.waterworld." + type.id()));
    }

    public int storedEnergy() {
        return energy;
    }

    public int storedWater() {
        return water == null ? 0 : water.getFluidAmount();
    }

    public int workStatus() {
        return workStatus;
    }

    public boolean mayDropMachine(ServerLevel server) {
        return identity != null && (removalOrigin || (!retiring
                && !OxygenMachineRemovalData.get(server).contains(identity, worldPosition)));
    }

    public void dismantle(ServerLevel server, BlockState oldState) {
        if (identity == null || retiring) {
            return;
        }
        OxygenMachineRemovalData removals = OxygenMachineRemovalData.get(server);
        boolean firstRemoval = !removals.contains(identity, worldPosition);
        retiring = true;
        removalOrigin = firstRemoval;
        if (tanks != null) {
            for (int slot = 0; slot < tanks.getContainerSize(); slot++) {
                ItemStack stack = tanks.removeItemNoUpdate(slot);
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(server, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
                }
            }
            setChanged();
        }
        removals.complete(identity, worldPosition);
        if (!firstRemoval) {
            return;
        }
        BlockPos root = OxygenMachineBlock.corePosition(worldPosition, oldState);
        for (int part = 0; part < type.size(); part++) {
            BlockPos pos = OxygenMachineBlock.position(root, oldState, part);
            if (!pos.equals(worldPosition)) {
                removals.enqueue(identity, pos);
            }
        }
        for (int part = 0; part < type.size(); part++) {
            BlockPos pos = OxygenMachineBlock.position(root, oldState, part);
            LevelChunk chunk = loadedChunk(server, pos);
            if (!pos.equals(worldPosition) && chunk != null) {
                OxygenMachineBlockEntity member = match(chunk, pos, oldState.setValue(OxygenMachineBlock.PART, part));
                if (member == null) {
                    removals.complete(identity, pos);
                } else {
                    member.removePart(server, pos);
                }
            }
        }
    }

    private void removePart(ServerLevel server, BlockPos pos) {
        server.setBlock(pos, getBlockState().getFluidState().createLegacyBlock(),
                Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        identity = tag.hasUUID("Structure") ? tag.getUUID("Structure") : null;
        if (tanks != null) {
            energy = Mth.clamp(tag.getInt("Energy"), 0, type.energyCapacity());
            NonNullList<ItemStack> contents = NonNullList.withSize(type.size(), ItemStack.EMPTY);
            ContainerHelper.loadAllItems(tag.getCompound("Tanks"), contents);
            for (int slot = 0; slot < contents.size(); slot++) {
                ItemStack stack = contents.get(slot);
                tanks.setItem(slot, OxygenTankStorage.isTank(stack) ? stack.copyWithCount(1) : ItemStack.EMPTY);
            }
        }
        if (water != null) {
            FluidStack saved = FluidStack.loadFluidStackFromNBT(tag.getCompound("Water"));
            water.setFluid(water.isFluidValid(saved)
                    ? new FluidStack(Fluids.WATER, Mth.clamp(saved.getAmount(), 0, type.waterCapacity())) : FluidStack.EMPTY);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (identity != null) {
            tag.putUUID("Structure", identity);
        }
        if (tanks != null) {
            tag.putInt("Energy", energy);
            NonNullList<ItemStack> contents = NonNullList.withSize(type.size(), ItemStack.EMPTY);
            for (int slot = 0; slot < contents.size(); slot++) {
                contents.set(slot, tanks.getItem(slot));
            }
            tag.put("Tanks", ContainerHelper.saveAllItems(new CompoundTag(), contents));
        }
        if (water != null) {
            tag.put("Water", water.getFluid().writeToNBT(new CompoundTag()));
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (!isRemoved() && !retiring) {
            if (capability == ForgeCapabilities.ENERGY) {
                return side == null ? energyView.cast() : side == OxygenMachineBlock.outerSide(getBlockState())
                        ? energyInput.cast() : LazyOptional.empty();
            }
            if (capability == ForgeCapabilities.FLUID_HANDLER && type.usesWater()) {
                return side == null ? waterView.cast()
                        : side == getBlockState().getValue(OxygenMachineBlock.FACING).getOpposite()
                        ? waterInput.cast() : LazyOptional.empty();
            }
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        capabilityGeneration++;
        energyInput.invalidate();
        energyView.invalidate();
        waterInput.invalidate();
        waterView.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyInput = LazyOptional.of(() -> new EnergyPort(true));
        energyView = LazyOptional.of(() -> new EnergyPort(false));
        waterInput = LazyOptional.of(() -> new WaterPort(true));
        waterView = LazyOptional.of(() -> new WaterPort(false));
    }

    private final class EnergyPort implements IEnergyStorage {
        private final boolean input;
        private final int generation = capabilityGeneration;

        private EnergyPort(boolean input) {
            this.input = input;
        }

        @Nullable
        private OxygenMachineBlockEntity resolve() {
            return generation == capabilityGeneration ? core() : null;
        }

        @Override
        public int receiveEnergy(int amount, boolean simulate) {
            OxygenMachineBlockEntity target = input && amount > 0 ? resolve() : null;
            if (target == null) {
                return 0;
            }
            int accepted = Math.min(amount, type.energyCapacity() - target.energy);
            if (!simulate && accepted > 0) {
                target.energy += accepted;
                target.setChanged();
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int amount, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            OxygenMachineBlockEntity target = resolve();
            return target == null ? 0 : target.energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return resolve() == null ? 0 : type.energyCapacity();
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return input && resolve() != null;
        }
    }

    private final class WaterPort implements IFluidHandler {
        private final boolean input;
        private final int generation = capabilityGeneration;

        private WaterPort(boolean input) {
            this.input = input;
        }

        @Nullable
        private OxygenMachineBlockEntity resolve() {
            return generation == capabilityGeneration ? core() : null;
        }

        @Nullable
        private FluidTank tank() {
            OxygenMachineBlockEntity target = resolve();
            return target == null ? null : target.water;
        }

        @Override
        public int getTanks() {
            return tank() == null ? 0 : 1;
        }

        @Override
        public FluidStack getFluidInTank(int index) {
            FluidTank target = index == 0 ? tank() : null;
            return target == null ? FluidStack.EMPTY : target.getFluid().copy();
        }

        @Override
        public int getTankCapacity(int index) {
            return index == 0 && tank() != null ? type.waterCapacity() : 0;
        }

        @Override
        public boolean isFluidValid(int index, FluidStack stack) {
            return input && index == 0 && stack.getFluid() == Fluids.WATER && tank() != null;
        }

        @Override
        public int fill(FluidStack stack, FluidAction action) {
            OxygenMachineBlockEntity target = input ? resolve() : null;
            if (target == null || target.water == null || stack.isEmpty() || stack.getFluid() != Fluids.WATER) {
                return 0;
            }
            int accepted = target.water.fill(new FluidStack(Fluids.WATER, stack.getAmount()), action);
            if (action.execute() && accepted > 0) {
                target.setChanged();
            }
            return accepted;
        }

        @Override
        public FluidStack drain(FluidStack stack, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int amount, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
