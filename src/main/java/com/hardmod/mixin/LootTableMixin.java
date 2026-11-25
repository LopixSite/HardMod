package com.hardmod.mixin;

import com.hardmod.loot.HardModLootNerfer;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

@Mixin(LootTable.class)
public abstract class LootTableMixin {

    /**
     * Tüm sandık, yapı ve mob ganimetlerini ciddi oranda azaltır (Loot Nerf):
     * - %60 ihtimalle eşya tamamen düşmez.
     * - Kalan eşyaların miktarı da yarıdan fazlası silinerek minimuma indirilir.
     */
    @ModifyVariable(
        method = "generateLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V",
        at = @At("HEAD"),
        argsOnly = true
    )
    private Consumer<ItemStack> hardmod$nerfLootConsumer(Consumer<ItemStack> original, LootContext context) {
        return stack -> {
            if (stack == null || stack.isEmpty()) {
                return;
            }

            // %60 ihtimalle bu loot hiç verilmez
            if (!HardModLootNerfer.shouldKeepLoot(context.getRandom())) {
                return;
            }

            // Kalan loot miktarı azaltılır
            HardModLootNerfer.nerfItemStack(stack);
            original.accept(stack);
        };
    }
}
