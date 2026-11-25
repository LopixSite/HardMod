package com.hardmod.mixin;

import com.hardmod.sculk.SculkInfectionHelper;
import net.minecraft.world.gen.feature.FeaturePlaceContext;
import net.minecraft.world.gen.feature.OreFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OreFeature.class)
public abstract class OreFeatureMixin {

    /**
     * 1. Maden Kıtlığı: Maden damarlarının %70'inin oluşumu engellenir.
     * 2. Sculk Sıklığı: İptal edilen madenlerin yerinde derinliklerde %30 ihtimalle yeni Sculk alanları filizlenir.
     */
    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    private void hardmod$reduceOresAndSpawnSculk(FeaturePlaceContext<OreFeatureConfig> context, CallbackInfoReturnable<Boolean> cir) {
        if (context.getRandom().nextFloat() < 0.70F) {
            // Y < 10 olan derin maden alanlarında sculk yayılımı sıklığını artır
            if (context.getOrigin().getY() < 10 && context.getRandom().nextFloat() < 0.30F) {
                SculkInfectionHelper.expandSculkArea(context.getWorld(), context.getOrigin(), context.getRandom(), 10);
            }

            cir.setReturnValue(false);
        }
    }
}
