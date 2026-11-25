package com.hardmod.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {

    protected VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * Köylülerin niteliklerine saldırı hasarı (Attack Damage) ekler.
     */
    @Inject(method = "createVillagerAttributes", at = @At("RETURN"), cancellable = true)
    private static void hardmod$addAttackDamage(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.setReturnValue(cir.getReturnValue().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0D));
    }

    /**
     * Köylüye yakın dövüş saldırı yapay zekası (MeleeAttackGoal) kazandırır.
     */
    @Inject(method = "initGoals", at = @At("TAIL"))
    private void hardmod$addAttackGoal(CallbackInfo ci) {
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.3D, false));
    }
}
