package com.hardmod.mixin;

import com.hardmod.sculk.SculkInfectionHelper;
import net.minecraft.world.gen.feature.FeaturePlaceContext;
import net.minecraft.world.gen.feature.SculkPatchFeature;
import net.minecraft.world.gen.feature.SculkPatchFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SculkPatchFeature.class)
public abstract class SculkPatchFeatureMixin {

    /**
     * Doğal Sculk alanlarını çok daha devasa hale getirir (Sculk Enlarge):
     * Deep Dark veya yer altındaki her sculk yaması genişletilerek devasa alanlara yayılır.
     */
    @Inject(method = "generate", at = @At("RETURN"))
    private void hardmod$enlargeSculkArea(FeaturePlaceContext<SculkPatchFeatureConfig> context, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            // 18 blok yarıçaplı devasa yayılma
            SculkInfectionHelper.expandSculkArea(context.getWorld(), context.getOrigin(), context.getRandom(), 18);
        }
    }
}
