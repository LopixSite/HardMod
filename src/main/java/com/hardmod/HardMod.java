package com.hardmod;

import com.hardmod.block.HardModBlocks;
import com.hardmod.mob.MobDifficultyManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HardMod implements ModInitializer {
    public static final String MOD_ID = "hardmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[HardMod] Mod altyapisi yukleniyor...");
        HardModBlocks.register();
        HardModEvents.register();
        MobDifficultyManager.register();
        LOGGER.info("[HardMod] TEBRIKLER! Tum HardMod mekanikleri (Faz 1-5) basariyla aktif edildi!");
    }
}
