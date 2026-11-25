package com.hardmod.torch;

import com.hardmod.block.HardModBlocks;
import com.hardmod.block.UnlitTorchBlock;
import com.hardmod.block.UnlitWallTorchBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TorchBurnoutManager {

    // 5 dakika = 300 saniye = 6000 tick
    public static final long BURNOUT_TICKS = 6000L;

    private static final Map<BlockPos, Long> ACTIVE_TORCHES = new ConcurrentHashMap<>();

    public static void registerTorch(BlockPos pos, long currentWorldTime) {
        ACTIVE_TORCHES.put(pos.toImmutable(), currentWorldTime + BURNOUT_TICKS);
    }

    public static void removeTorch(BlockPos pos) {
        ACTIVE_TORCHES.remove(pos);
    }

    /**
     * Her saniye (20 tick) aktif meşaleleri kontrol eder.
     * Süresi dolan (5 dakika) meşaleleri söndürür.
     */
    public static void tick(ServerWorld world) {
        long currentTime = world.getTime();
        Iterator<Map.Entry<BlockPos, Long>> iterator = ACTIVE_TORCHES.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Long> entry = iterator.next();
            BlockPos pos = entry.getKey();
            long expiryTime = entry.getValue();

            // Chunk yüklü değilse bekle
            if (!world.isChunkLoaded(pos)) {
                continue;
            }

            BlockState state = world.getBlockState(pos);

            // Blok artık meşale değilse takipten çıkar
            if (!state.isOf(Blocks.TORCH) && !state.isOf(Blocks.WALL_TORCH)) {
                iterator.remove();
                continue;
            }

            // 5 dakika dolduysa söndür
            if (currentTime >= expiryTime) {
                iterator.remove();
                extinguishTorch(world, pos, state);
            }
        }
    }

    private static void extinguishTorch(ServerWorld world, BlockPos pos, BlockState state) {
        // Duman ve sönme sesi
        world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.7F, 1.2F);
        world.spawnParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 6, 0.1, 0.1, 0.1, 0.02);

        if (state.isOf(Blocks.WALL_TORCH)) {
            Direction facing = state.get(WallTorchBlock.FACING);
            world.setBlockState(pos, HardModBlocks.UNLIT_WALL_TORCH.getDefaultState().with(UnlitWallTorchBlock.FACING, facing), Block.NOTIFY_ALL);
        } else {
            world.setBlockState(pos, HardModBlocks.UNLIT_TORCH.getDefaultState(), Block.NOTIFY_ALL);
        }
    }
}
