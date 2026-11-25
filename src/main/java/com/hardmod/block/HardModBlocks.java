package com.hardmod.block;

import com.hardmod.HardMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class HardModBlocks {

    public static final Block UNLIT_TORCH = new UnlitTorchBlock(
        AbstractBlock.Settings.create()
            .noCollision()
            .breakInstantly()
            .sounds(BlockSoundGroup.WOOD)
            .pistonBehavior(PistonBehavior.DESTROY)
    );

    public static final Block UNLIT_WALL_TORCH = new UnlitWallTorchBlock(
        AbstractBlock.Settings.create()
            .noCollision()
            .breakInstantly()
            .sounds(BlockSoundGroup.WOOD)
            .pistonBehavior(PistonBehavior.DESTROY)
    );

    public static void register() {
        Registry.register(Registries.BLOCK, Identifier.of(HardMod.MOD_ID, "unlit_torch"), UNLIT_TORCH);
        Registry.register(Registries.BLOCK, Identifier.of(HardMod.MOD_ID, "unlit_wall_torch"), UNLIT_WALL_TORCH);
    }
}
