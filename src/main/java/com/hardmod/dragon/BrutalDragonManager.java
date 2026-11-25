package com.hardmod.dragon;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

public class BrutalDragonManager {

    /**
     * Ejderha savaşındaki oyuncuları cezalandıran ve savaşı kabusa çeviren mantık:
     * 1. Minion Çağırma: Her 15 saniyede bir oyuncuların yakınına öfkeli Endermite minionları çağırır.
     * 2. Portal Gazabı: Ejderha kaideye (bedrock portal) indiğinde 24 blok alana Wither ve Slowness saçar.
     * 3. Şimşek Saldırısı: Yüksek kulelerde kamp yapan oyuncuların üzerine şimşek düşürür.
     */
    public static void tickDragonFight(EnderDragonEntity dragon) {
        if (dragon.getWorld().isClient() || !(dragon.getWorld() instanceof ServerWorld world)) {
            return;
        }

        long time = world.getTime();

        // 1. Minion Çağırma (Her 300 tick = 15 saniyede bir)
        if (time % 300L == 0L) {
            Box fightBox = dragon.getBoundingBox().expand(100.0, 50.0, 100.0);
            List<ServerPlayerEntity> players = world.getEntitiesByClass(ServerPlayerEntity.class, fightBox, p -> !p.isCreative() && !p.isSpectator());

            for (ServerPlayerEntity player : players) {
                // Minion olarak oyuncunun dibine 2 adet Endermite çağır
                for (int i = 0; i < 2; i++) {
                    EndermiteEntity minion = EntityType.ENDERMITE.create(world, SpawnReason.EVENT);
                    if (minion != null) {
                        minion.refreshPositionAndAngles(
                            player.getX() + (world.random.nextDouble() - 0.5) * 4.0,
                            player.getY(),
                            player.getZ() + (world.random.nextDouble() - 0.5) * 4.0,
                            world.random.nextFloat() * 360.0F,
                            0.0F
                        );
                        minion.setTarget(player);
                        world.spawnEntity(minion);
                    }
                }

                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_SCREAM, SoundCategory.HOSTILE, 1.0F, 1.2F);
                player.sendMessage(Text.literal("§4[HardMod] §cEnder Dragon minyonlarını üzerine saldı!"), true);
            }
        }

        // 2. Kulede Kamp Yapanlara Şimşek Cezası (Y > 90 olan oyuncular)
        if (time % 100L == 0L) {
            Box fightBox = dragon.getBoundingBox().expand(80.0, 80.0, 80.0);
            List<ServerPlayerEntity> players = world.getEntitiesByClass(ServerPlayerEntity.class, fightBox, p -> p.getY() > 90.0 && !p.isCreative());

            for (ServerPlayerEntity camper : players) {
                EntityType.LIGHTNING_BOLT.spawn(world, camper.getBlockPos(), SpawnReason.TRIGGERED);
                camper.sendMessage(Text.literal("§4[HardMod] §cEjderha kuledeki kampını şimşekle cezalandırdı!"), true);
            }
        }

        // 3. Kaideye İndiğinde Ölümcül Alan Darbesi (Wither II + Körlük)
        Box portalArea = new Box(-20.0, 50.0, -20.0, 20.0, 90.0, 20.0);
        if (portalArea.intersects(dragon.getBoundingBox())) {
            List<ServerPlayerEntity> nearby = world.getEntitiesByClass(ServerPlayerEntity.class, portalArea.expand(10.0), p -> !p.isCreative());
            for (ServerPlayerEntity player : nearby) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 80, 1, false, true, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 1, false, true, true));
                world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
            }
        }
    }
}
