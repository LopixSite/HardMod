package com.hardmod.sculk;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SculkShriekerBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;

public class SculkInfectionHelper {

    /**
     * Belirtilen merkez etrafında devasa bir Sculk alanı oluşturur.
     * Sculk blokları, sensörler, katalizörler ve can_summon özellikli shrieker'lar yerleştirir.
     */
    public static void expandSculkArea(StructureWorldAccess world, BlockPos center, Random random, int radius) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -6; y <= 6; y++) {
                    if (x * x + z * z + y * y * 2 > radius * radius) {
                        continue;
                    }

                    mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    BlockState current = world.getBlockState(mutable);

                    // Taş, deepslate, kumtaşı vb. katı zemin bloklarını sculk ile enfekte et
                    if (current.isOpaqueFullCube(world, mutable) && !current.isOf(Blocks.BEDROCK)) {
                        BlockPos above = mutable.up();
                        BlockState aboveState = world.getBlockState(above);

                        // Eğer üstü hava veya sıvı ise yüzey sculk dekorasyonu ekle
                        if (aboveState.isAir()) {
                            float roll = random.nextFloat();
                            if (roll < 0.65F) {
                                world.setBlockState(mutable, Blocks.SCULK.getDefaultState(), 3);
                            } else if (roll < 0.75F) {
                                world.setBlockState(mutable, Blocks.SCULK.getDefaultState(), 3);
                                world.setBlockState(above, Blocks.SCULK_SENSOR.getDefaultState(), 3);
                            } else if (roll < 0.85F) {
                                world.setBlockState(mutable, Blocks.SCULK.getDefaultState(), 3);
                                world.setBlockState(above, Blocks.SCULK_CATALYST.getDefaultState(), 3);
                            } else if (roll < 0.95F) {
                                world.setBlockState(mutable, Blocks.SCULK.getDefaultState(), 3);
                                // Warden çağırabilen aktif Sculk Shrieker
                                world.setBlockState(above, Blocks.SCULK_SHRIEKER.getDefaultState().with(SculkShriekerBlock.CAN_SUMMON, true), 3);
                            }
                        } else {
                            // Derin kısımları saf sculk bloğuna çevir
                            if (random.nextFloat() < 0.80F) {
                                world.setBlockState(mutable, Blocks.SCULK.getDefaultState(), 3);
                            }
                        }
                    }
                }
            }
        }
    }
}
