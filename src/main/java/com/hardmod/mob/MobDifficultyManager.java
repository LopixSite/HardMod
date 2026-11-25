package com.hardmod.mob;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import java.util.List;

public class MobDifficultyManager {

    /**
     * Faz 3 Başlatıcı:
     * - Mobların görüş mesafesini (follow range) 2 katına çıkarır.
     */
    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof HostileEntity hostile) {
                EntityAttributeInstance followRange = hostile.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
                if (followRange != null) {
                    double current = followRange.getBaseValue();
                    // Takip mesafesini 2 katına çıkar
                    if (current < 100.0) {
                        followRange.setBaseValue(current * 2.0);
                    }
                }
            }
        });
    }

    /**
     * Bir köylüye vurulduğunda 32 blok çevredeki tüm köylüleri oyuncuya karşı saldırgan yapar.
     */
    public static void alertVillagers(VillagerEntity victim, PlayerEntity attacker) {
        if (attacker == null || victim.getWorld().isClient()) {
            return;
        }

        ServerWorld world = (ServerWorld) victim.getWorld();
        Box alertBox = victim.getBoundingBox().expand(32.0, 16.0, 32.0);
        List<VillagerEntity> nearbyVillagers = world.getEntitiesByClass(VillagerEntity.class, alertBox, v -> v.isAlive());

        attacker.sendMessage(Text.literal("§4§l[HardMod] §cKöylüler birleşti ve sana saldırıyor!"), true);

        for (VillagerEntity villager : nearbyVillagers) {
            // Öfke efekti ve ses
            world.spawnParticles(ParticleTypes.ANGRY_VILLAGER, villager.getX(), villager.getY() + 1.2, villager.getZ(), 5, 0.2, 0.2, 0.2, 0.0);
            world.playSound(null, villager.getBlockPos(), SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.NEUTRAL, 1.0F, 0.8F);

            // Köylüyü oyuncuya hedefle
            villager.setTarget(attacker);
        }
    }
}
