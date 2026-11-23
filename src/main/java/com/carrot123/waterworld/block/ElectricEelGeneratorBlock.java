package com.carrot123.waterworld.block;

import com.carrot123.waterworld.blockentity.ElectricEelGeneratorBlockEntity;
import com.carrot123.waterworld.registry.ModBlockEntities;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ElectricEelGeneratorBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty SECONDARY = BooleanProperty.create("secondary");

    public ElectricEelGeneratorBlock(Properties properties) {
        super(properties.pushReaction(PushReaction.BLOCK));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SECONDARY, false));
    }

    public static Direction outputSide(BlockState state) {
        Direction axis = state.getValue(FACING).getClockWise();
        return state.getValue(SECONDARY) ? axis : axis.getOpposite();
    }

    public static BlockPos partnerPos(BlockPos pos, BlockState state) {
        return pos.relative(outputSide(state).getOpposite());
    }

    public static boolean isPartner(BlockState state, BlockState other) {
        return other.is(state.getBlock()) && other.getValue(FACING) == state.getValue(FACING)
                && other.getValue(SECONDARY) != state.getValue(SECONDARY);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SECONDARY);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricEelGeneratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.ELECTRIC_EEL_GENERATOR.get(),
                ElectricEelGeneratorBlockEntity::serverTick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level instanceof ServerLevel server && !level.restoringBlockSnapshots
                && level.getBlockEntity(pos) instanceof ElectricEelGeneratorBlockEntity generator) {
            generator.removePartner(server, state);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof ElectricEelGeneratorBlockEntity generator
                && generator.mayDrop(builder.getLevel())) {
            return super.getDrops(state, builder);
        }
        return List.of();
    }
}
