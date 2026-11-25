package com.hardmod.block;

import com.hardmod.torch.TorchBurnoutManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class UnlitTorchBlock extends Block {

    protected static final VoxelShape STANDING_SHAPE = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 10.0, 10.0);

    public UnlitTorchBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return STANDING_SHAPE;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !this.canPlaceAt(state, world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return sideCoversSmallSquare(world, pos.down(), Direction.UP);
    }

    /**
     * Sönük meşaleye tıklandığında çakmak (Flint and Steel) kontrolü:
     * - Çakmak varsa yakar, çakmağın canını azaltır ve 5 dakikalık yeni süre başlatır.
     * - Çakmak yoksa uyarı mesajı verir.
     */
    public ActionResult handleTorchUse(World world, BlockPos pos, PlayerEntity player, Hand hand) {
        ItemStack held = player.getStackInHand(hand);

        if (held.isOf(Items.FLINT_AND_STEEL)) {
            world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
            
            if (!world.isClient()) {
                world.setBlockState(pos, Blocks.TORCH.getDefaultState(), Block.NOTIFY_ALL);
                TorchBurnoutManager.registerTorch(pos, world.getTime());

                held.damage(1, player, EquipmentSlot.MAINHAND);
                player.sendMessage(Text.literal("§6[HardMod] §eMeşale çakmak ile yeniden yakıldı! (5 dakika süresi var)"), true);
            } else {
                for (int i = 0; i < 5; i++) {
                    world.addParticle(
                        ParticleTypes.FLAME,
                        pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.2,
                        pos.getY() + 0.6 + world.random.nextDouble() * 0.2,
                        pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.2,
                        0.0, 0.05, 0.0
                    );
                }
            }
            return ActionResult.SUCCESS;
        } else {
            if (!world.isClient()) {
                player.sendMessage(Text.literal("§8[HardMod] §7Bu meşale sönmüş. Tekrar yakmak için elinde §fÇakmak (Flint and Steel) §7olmalı!"), true);
            }
            return ActionResult.CONSUME;
        }
    }
}
