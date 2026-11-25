package com.hardmod.block;

import com.hardmod.torch.TorchBurnoutManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.util.Map;

public class UnlitWallTorchBlock extends Block {

    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    private static final Map<Direction, VoxelShape> SHAPES = WallTorchBlock.getBoundingShapes();

    public UnlitWallTorchBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return WallTorchBlock.getBoundingShape(state);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos blockPos = pos.offset(direction.getOpposite());
        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isSideSolidFullSquare(world, blockPos, direction);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockState = this.getDefaultState();
        WorldView worldView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        Direction[] directions = ctx.getPlacementDirections();

        for (Direction direction : directions) {
            if (direction.getAxis().isHorizontal()) {
                Direction opposite = direction.getOpposite();
                blockState = blockState.with(FACING, opposite);
                if (blockState.canPlaceAt(worldView, blockPos)) {
                    return blockState;
                }
            }
        }
        return null;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public ActionResult handleTorchUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand) {
        ItemStack held = player.getStackInHand(hand);

        if (held.isOf(Items.FLINT_AND_STEEL)) {
            world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);

            if (!world.isClient()) {
                Direction facing = state.get(FACING);
                world.setBlockState(pos, Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, facing), Block.NOTIFY_ALL);
                TorchBurnoutManager.registerTorch(pos, world.getTime());

                held.damage(1, player, EquipmentSlot.MAINHAND);
                player.sendMessage(Text.literal("§6[HardMod] §eDuvar meşalesi çakmak ile yeniden yakıldı! (5 dakika süresi var)"), true);
            } else {
                for (int i = 0; i < 5; i++) {
                    world.addParticle(
                        ParticleTypes.FLAME,
                        pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.2,
                        pos.getY() + 0.7 + world.random.nextDouble() * 0.2,
                        pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.2,
                        0.0, 0.05, 0.0
                    );
                }
            }
            return ActionResult.SUCCESS;
        } else {
            if (!world.isClient()) {
                player.sendMessage(Text.literal("§8[HardMod] §7Bu duvar meşalesi sönmüş. Tekrar yakmak için elinde §fÇakmak (Flint and Steel) §7olmalı!"), true);
            }
            return ActionResult.CONSUME;
        }
    }
}
