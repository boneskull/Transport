package xyz.brassgoggledcoders.transport.block;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

public class ScaffoldingSlabBlock extends SlabBlock {
    public static final IntegerProperty DISTANCE_07 = BlockStateProperties.DISTANCE_0_7;
    public static final BooleanProperty RAILED = BooleanProperty.create("railed");

    private static final VoxelShape field_220121_d;
    private static final VoxelShape field_220123_f = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    private static final VoxelShape field_220124_g = VoxelShapes.fullCube().withOffset(0.0D, -1.0D, 0.0D);

    static {
        VoxelShape voxelShape = Block.makeCuboidShape(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        VoxelShape voxelShape1 = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 2.0D);
        VoxelShape voxelShape2 = Block.makeCuboidShape(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D);
        VoxelShape voxelShape3 = Block.makeCuboidShape(0.0D, 0.0D, 14.0D, 2.0D, 16.0D, 16.0D);
        VoxelShape voxelShape4 = Block.makeCuboidShape(14.0D, 0.0D, 14.0D, 16.0D, 16.0D, 16.0D);
        field_220121_d = VoxelShapes.or(voxelShape, voxelShape1, voxelShape2, voxelShape3, voxelShape4);
    }

    public ScaffoldingSlabBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.getDefaultState().with(RAILED, false));
    }

    public static int getDistance(IBlockReader blockReader, BlockPos blockPos) {
        BlockPos.Mutable mutableBlockPos = blockPos.toMutable().move(Direction.DOWN);
        BlockState blockState = blockReader.getBlockState(mutableBlockPos);
        int i = 7;
        if (blockState.getBlock() instanceof ScaffoldingSlabBlock) {
            i = blockState.get(DISTANCE_07);
        } else if (blockState.getBlock() instanceof ScaffoldingBlock) {
            i = blockState.get(ScaffoldingBlock.DISTANCE);
        } else if (blockState.isSolidSide(blockReader, mutableBlockPos, Direction.UP)) {
            return 0;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState nextCheckedState = blockReader.getBlockState(mutableBlockPos.setPos(blockPos).move(direction));
            if (nextCheckedState.getBlock() instanceof ScaffoldingBlock) {
                i = Math.min(i, nextCheckedState.get(ScaffoldingBlock.DISTANCE) + 1);
                if (i == 1) {
                    break;
                }
            } else if (nextCheckedState.getBlock() instanceof ScaffoldingSlabBlock) {
                i = Math.min(i, nextCheckedState.get(DISTANCE_07) + 1);
                if (i == 1) {
                    break;
                }
            }
        }

        return i;
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(DISTANCE_07, RAILED);
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld world,
                                          BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            world.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        if (!world.isRemote()) {
            world.getPendingBlockTicks().scheduleTick(currentPos, this, 1);
        }

        return stateIn;
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        return getDistance(world, pos) < 7;
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        if (context.getEntity() instanceof AbstractMinecartEntity) {
            return VoxelShapes.empty();
        } else {
            switch (state.get(TYPE)) {
                case DOUBLE:
                    if (context.func_216378_a(VoxelShapes.fullCube(), pos, true)) {
                        return field_220121_d;
                    } else {
                        return state.get(DISTANCE_07) != 0 && context.func_216378_a(field_220124_g, pos, true) ?
                                field_220123_f : VoxelShapes.empty();
                    }
                case TOP:
                    return TOP_SHAPE;
                case BOTTOM:
                    return BOTTOM_SHAPE;
            }
            return super.getCollisionShape(state, world, pos, context);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!worldIn.isRemote) {
            worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        int i = getDistance(world, pos);
        BlockState blockstate = state.with(DISTANCE_07, i);
        if (blockstate.get(DISTANCE_07) == 7) {
            if (state.get(DISTANCE_07) == 7) {
                world.addEntity(new FallingBlockEntity(world, (double) pos.getX() + 0.5D, pos.getY(),
                        (double) pos.getZ() + 0.5D, blockstate.with(WATERLOGGED, Boolean.FALSE)));
            } else {
                world.destroyBlock(pos, true);
            }
        } else if (state != blockstate) {
            world.setBlockState(pos, blockstate, 3);
        }

    }

    @Override
    public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
        return state.get(TYPE) == SlabType.DOUBLE;
    }
}
