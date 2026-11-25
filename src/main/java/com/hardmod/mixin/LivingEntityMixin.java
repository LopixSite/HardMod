package com.hardmod.mixin;

import com.hardmod.mob.MobDifficultyManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * Düşman mobların hasarını katla (Normal zordan kat kat daha fazla vurması için 2.5 kat hasar).
     */
    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float hardmod$multiplyMonsterDamage(float amount, DamageSource source) {
        LivingEntity target = (LivingEntity) (Object) this;

        // Canavarlar oyuncuya vurduğunda hasar çarpanı uygula
        if (target instanceof PlayerEntity && source.getAttacker() instanceof Monster) {
            return amount * 2.5F;
        }

        return amount;
    }

    /**
     * Bir köylüye oyuncu tarafından vurulduğunda çevredeki tüm köylüleri alarma geçir.
     */
    @Inject(method = "damage", at = @At("TAIL"))
    private void hardmod$onVillagerDamaged(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (cir.getReturnValue() && entity instanceof VillagerEntity villager) {
            if (source.getAttacker() instanceof PlayerEntity player) {
                MobDifficultyManager.alertVillagers(villager, player);
            }
        }
    }
}
