package xyz.brassgoggledcoders.transport.block.boat;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import xyz.brassgoggledcoders.transport.api.TransportCapabilities;
import xyz.brassgoggledcoders.transport.api.navigation.INavigationPoint;
import xyz.brassgoggledcoders.transport.content.TransportBlocks;
import xyz.brassgoggledcoders.transport.content.TransportNavigationPoints;
import xyz.brassgoggledcoders.transport.tileentity.boat.BuoyTileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

public class BuoyBlock extends Block {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public static final VoxelShape TOP = VoxelShapes.or(
            VoxelShapes.create(0.375, 0.65625, 0.375, 0.625, 0.96875, 0.625),
            VoxelShapes.create(0.21875, 0, 0.21875, 0.78125, 0.6875, 0.78125)
    );

    public static final VoxelShape BOTTOM = VoxelShapes.or(
            VoxelShapes.create(0.21875, 0.375, 0.21875, 0.78125, 1, 0.78125),
            VoxelShapes.create(0.1875, 0.25, 0.1875, 0.8125, 0.375, 0.8125),
            VoxelShapes.create(0.03125, 0, 0.03125, 0.96875, 0.25, 0.96875)
    );

    public BuoyBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.getStateContainer().getBaseState().with(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@Nonnull BlockItemUseContext context) {
        if (context.getWorld().getBlockState(context.getPos().up()).isReplaceable(context)) {
            return this.getDefaultState();
        } else {
            return null;
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER), 3);
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof BuoyTileEntity) {
            ((BuoyTileEntity) tileEntity).setup(placer instanceof PlayerEntity ? (PlayerEntity)placer : null);
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.isIn(newState.getBlock())) {
            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity instanceof BuoyTileEntity) {
                ((BuoyTileEntity) tileentity).destroy();
            }

            super.onReplaced(state, world, pos, newState, isMoving);
        }
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    @SuppressWarnings("deprecation")
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
                                             Hand hand, BlockRayTraceResult hit) {
        BlockPos blockPos = pos;
        if (state.get(HALF) == DoubleBlockHalf.UPPER) {
            blockPos = blockPos.down();
        }
        TileEntity tileEntity = world.getTileEntity(blockPos);
        if (tileEntity instanceof BuoyTileEntity) {
            ((BuoyTileEntity) tileEntity).interact(player);
            return ActionResultType.SUCCESS;
        } else {
            return ActionResultType.FAIL;
        }
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if (context.getEntity() instanceof BoatEntity) {
            return VoxelShapes.empty();
        } else if (state.get(HALF) == DoubleBlockHalf.UPPER) {
            return TOP;
        } else {
            return BOTTOM;
        }
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public BlockState updatePostPlacement(BlockState blockState, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        return !blockState.isValidPosition(world, currentPos) ? Blocks.AIR.getDefaultState() :
                super.updatePostPlacement(blockState, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public boolean isValidPosition(BlockState blockState, IWorldReader worldReader, BlockPos blockPos) {
        BlockPos downBlockPos = blockPos.down();
        if (blockState.get(HALF) == DoubleBlockHalf.LOWER) {
            return worldReader.getFluidState(downBlockPos).getFluid().isIn(FluidTags.WATER);
        } else {
            return worldReader.getBlockState(downBlockPos).getBlock() == this;
        }
    }

    public static int getLightLevel(BlockState blockState) {
        return blockState.get(BuoyBlock.HALF) == DoubleBlockHalf.UPPER ? 5 : 0;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return state.get(HALF) == DoubleBlockHalf.LOWER ? new BuoyTileEntity(TransportBlocks.BUOY_TILE_ENTITY.get()) : null;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.get(HALF) == DoubleBlockHalf.LOWER;
    }
}
