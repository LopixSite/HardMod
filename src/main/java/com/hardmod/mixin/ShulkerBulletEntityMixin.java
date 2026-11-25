package com.hardmod.mixin;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ShulkerBulletEntity.class)
public abstract class ShulkerBulletEntityMixin {

    /**
     * Shulker mermisinin verdiği Levitation efektini 10 saniye yerine
     * 1 dakika (60 saniye = 1200 tick) yapar.
     */
    @ModifyArg(
        method = "onEntityHit",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z"),
        index = 0
    )
    private StatusEffectInstance hardmod$extendLevitation(StatusEffectInstance original) {
        if (original.getEffectType() == StatusEffects.LEVITATION) {
            // 1 dakika = 60 saniye * 20 tick = 1200 tick
            return new StatusEffectInstance(StatusEffects.LEVITATION, 1200, original.getAmplifier());
        }
        return original;
    }
}
