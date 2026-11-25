package com.hardmod;

import com.hardmod.block.HardModBlocks;
import com.hardmod.block.UnlitTorchBlock;
import com.hardmod.block.UnlitWallTorchBlock;
import com.hardmod.curse.CurseManager;
import com.hardmod.torch.TorchBurnoutManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.ExplosionBehavior;

public class HardModEvents {

    private static final String FIRST_JOIN_TAG = "hardmod_started";
    private static final long MIDNIGHT_TIME = 18000L;

    public static void register() {
        registerAlwaysNight();
        registerPlayerStartingKit();
        registerBedExplosions();
        registerPhase2Mechanics();
    }

    /**
     * Faz 1: Her zaman gece olması (Always Night)
     */
    private static void registerAlwaysNight() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
            if (world.getRegistryKey() == World.OVERWORLD) {
                world.setTimeOfDay(MIDNIGHT_TIME);
            }
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getRegistryKey() == World.OVERWORLD) {
                long time = world.getTimeOfDay() % 24000L;
                if (time < 13000L || time > 23000L) {
                    world.setTimeOfDay(MIDNIGHT_TIME);
                }
            }
        });
    }

    /**
     * Faz 1: Başlangıç Kiti & 30dk Kötü Şans Efekti
     */
    private static void registerPlayerStartingKit() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();

            if (!player.getCommandTags().contains(FIRST_JOIN_TAG)) {
                player.addCommandTag(FIRST_JOIN_TAG);

                // 3 Toprak ve 3 Meşe Fidanı
                ItemStack dirt = new ItemStack(Items.DIRT, 3);
                ItemStack sapling = new ItemStack(Items.OAK_SAPLING, 3);

                if (!player.getInventory().insertStack(dirt)) {
                    player.dropItem(dirt, false);
                }
                if (!player.getInventory().insertStack(sapling)) {
                    player.dropItem(sapling, false);
                }

                // 30 dakikalık Kötü Şans (Bad Luck)
                StatusEffectInstance badLuck = new StatusEffectInstance(
                    StatusEffects.UNLUCK,
                    36000,
                    0,
                    false,
                    true,
                    true
                );
                player.addStatusEffect(badLuck);

                player.sendMessage(Text.literal("§4§l[HardMod] §cKabus Başladı!"), false);
                player.sendMessage(Text.literal("§6- 3 Toprak ve 3 Meşe Fidanı verildi.\n§6- 30 Dakikalık Kötü Şans devrede!\n§4- Yataklar patlar, eşyalar lanetlenir ve meşaleler 5dk sonra söner!"), false);
            }
        });
    }

    /**
     * Faz 1: Yatakların Tüm Boyutlarda Patlaması
     */
    private static void registerBedExplosions() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (state.getBlock() instanceof BedBlock) {
                if (!world.isClient()) {
                    ServerWorld serverWorld = (ServerWorld) world;

                    world.removeBlock(pos, false);
                    BlockPos otherPartPos = pos.offset(
                        BedBlock.getDirectionTowardsOtherPart(
                            state.get(BedBlock.PART),
                            state.get(HorizontalFacingBlock.FACING)
                        )
                    );
                    if (world.getBlockState(otherPartPos).getBlock() instanceof BedBlock) {
                        world.removeBlock(otherPartPos, false);
                    }

                    Vec3d center = pos.toCenterPos();
                    serverWorld.createExplosion(
                        null,
                        serverWorld.getDamageSources().badRespawnPoint(center),
                        new ExplosionBehavior(),
                        center,
                        5.0F,
                        true,
                        World.ExplosionSourceType.BLOCK
                    );
                }
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });
    }

    /**
     * Faz 2: Envanter Kaybolma Laneti & Sönen Meşale Mekanikleri
     */
    private static void registerPhase2Mechanics() {
        // 1. Tick döngüsünde envanter laneti ve meşale söndürme takibi
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            // Her tick oyuncu envanterlerini kontrol et ve Kaybolma Laneti uygula
            for (ServerPlayerEntity player : world.getPlayers()) {
                CurseManager.processPlayerInventory(world, player);
            }

            // Her 20 tickte (1 saniye) bir meşale sürelerini kontrol et
            if (world.getTime() % 20L == 0L) {
                TorchBurnoutManager.tick(world);
            }
        });

        // 2. Sönük meşalelere sağ tıklama (Çakmak ile yakma mekanizması)
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (state.isOf(HardModBlocks.UNLIT_TORCH)) {
                return ((UnlitTorchBlock) state.getBlock()).handleTorchUse(world, pos, player, hand);
            }

            if (state.isOf(HardModBlocks.UNLIT_WALL_TORCH)) {
                return ((UnlitWallTorchBlock) state.getBlock()).handleTorchUse(world, pos, state, player, hand);
            }

            return ActionResult.PASS;
        });
    }
}
