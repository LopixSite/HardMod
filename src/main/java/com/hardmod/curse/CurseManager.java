package com.hardmod.curse;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class CurseManager {

    private static RegistryEntry<Enchantment> cachedCurseEntry = null;

    /**
     * Sunucu dünyası üzerinden Kaybolma Laneti (Vanishing Curse) referansını önbelleğe alır.
     */
    public static void updateCurseCache(ServerWorld world) {
        if (cachedCurseEntry == null) {
            var enchantmentRegistry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            enchantmentRegistry.getOptional(Enchantments.VANISHING_CURSE).ifPresent(entry -> cachedCurseEntry = entry);
        }
    }

    /**
     * Oyuncunun envanterindeki bütün eşyaları kontrol eder.
     * Envantere giren her eşyaya Kaybolma Laneti ekler.
     */
    public static void processPlayerInventory(ServerWorld world, ServerPlayerEntity player) {
        updateCurseCache(world);

        if (cachedCurseEntry == null) {
            return;
        }

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                applyCurseIfMissing(stack, cachedCurseEntry);
            }
        }
    }

    /**
     * Eşyaya Kaybolma Lanetini uygular.
     */
    public static void applyCurseIfMissing(ItemStack stack, RegistryEntry<Enchantment> curseEntry) {
        ItemEnchantmentsComponent enchantments = stack.getOrDefault(
            DataComponentTypes.ENCHANTMENTS,
            ItemEnchantmentsComponent.DEFAULT
        );

        if (enchantments.getLevel(curseEntry) <= 0) {
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(enchantments);
            builder.set(curseEntry, 1);
            stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        }
    }

    /**
     * Bileme taşı vb. yerlerden çıkan eşyalara lanetin kalıcı olarak geri eklenmesini sağlar.
     */
    public static void ensureCurseOnStack(ItemStack stack) {
        if (cachedCurseEntry != null && !stack.isEmpty()) {
            applyCurseIfMissing(stack, cachedCurseEntry);
        }
    }
}
