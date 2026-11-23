package com.carrot123.waterworld.block;

import com.carrot123.waterworld.blockentity.RecyclingStationBlockEntity;
import com.carrot123.waterworld.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RecyclingStationBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final double SURFACE_Y = 12.0D / 16.0D;
    private static final VoxelShape SHAPE = Shapes.or(box(0, 9, 0, 16, 12, 16),
            box(1, 0, 1, 4, 9, 4), box(12, 0, 1, 15, 9, 4),
            box(1, 0, 12, 4, 9, 15), box(12, 0, 12, 15, 9, 15));

    public RecyclingStationBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RecyclingStationBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return defaultBlockState().setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || !(level.getBlockEntity(pos) instanceof RecyclingStationBlockEntity station)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        boolean insert = hit.getDirection() == Direction.UP && held.is(ModItems.SCRAP_IRON.get());
        boolean hammer = held.is(ModItems.RECYCLING_HAMMER.get());
        boolean retrieve = held.isEmpty() && !station.isEmpty();
        if (!insert && !hammer && !retrieve) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            if (insert && station.insertScrap(held)) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
            } else if (retrieve) {
                ItemStack recovered = station.takeScrap();
                player.getInventory().add(recovered);
                drop(server, pos, recovered);
            } else if (hammer && !station.takeScrap().isEmpty()) {
                recycle(server, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void recycle(ServerLevel level, BlockPos pos) {
        RandomSource random = level.getRandom();
        int ironNuggets = 1 + random.nextInt(2);
        boolean gold = random.nextFloat() < 0.5F;
        boolean iron = random.nextFloat() < 0.12F;
        boolean screw = random.nextFloat() < 0.25F;
        drop(level, pos, new ItemStack(Items.IRON_NUGGET, ironNuggets));
        if (gold) {
            drop(level, pos, new ItemStack(Items.GOLD_NUGGET));
        }
        if (iron) {
            drop(level, pos, new ItemStack(Items.IRON_INGOT));
        }
        if (screw) {
            drop(level, pos, new ItemStack(ModItems.SCREW.get()));
        }
        level.playSound(null, pos, SoundEvents.ANVIL_HIT, SoundSource.BLOCKS, 0.45F, 1.1F);
    }

    private static void drop(ServerLevel level, BlockPos pos, ItemStack stack) {
        if (!stack.isEmpty()) {
            ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5D,
                    pos.getY() + SURFACE_Y + 0.03D, pos.getZ() + 0.5D, stack);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level instanceof ServerLevel server
                && level.getBlockEntity(pos) instanceof RecyclingStationBlockEntity station) {
            drop(server, pos, station.clearForRemoval());
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
