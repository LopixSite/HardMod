package com.hardmod.loot;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;

public class HardModLootNerfer {

    /**
     * Bütün lootların azaltılması (Loot Nerf):
     * - Sandıklar, yapılar ve mob düşmelerinde %60 ihtimalle eşya tamamen elenir.
     * - Kalan eşyaların miktarı da yarıdan fazlası kırılarak kıtlık yaratılır.
     */
    public static boolean shouldKeepLoot(Random random) {
        return random.nextFloat() >= 0.60F;
    }

    public static void nerfItemStack(ItemStack stack) {
        if (!stack.isEmpty()) {
            int originalCount = stack.getCount();
            int nerfedCount = Math.max(1, (int) Math.ceil(originalCount * 0.4));
            stack.setCount(nerfedCount);
        }
    }
}
