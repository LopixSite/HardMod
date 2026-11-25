package com.hardmod.mixin;

import net.minecraft.block.BedBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public abstract class BedBlockMixin {

    /**
     * Minecraft'ın yerleşik yatak patlama mekanizması:
     * isBedWorking(world) çağrısı false döndüğünde, yatak Overworld dahil
     * her boyutta otomatik olarak parçalanıp patlar (badRespawnPoint).
     */
    @Inject(method = "isBedWorking", at = @At("HEAD"), cancellable = true)
    private static void hardmod$forceBedExplosionEverywhere(World world, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
