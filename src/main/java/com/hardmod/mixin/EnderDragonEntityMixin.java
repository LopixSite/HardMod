package com.hardmod.mixin;

import com.hardmod.dragon.BrutalDragonManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin extends MobEntity {

    protected EnderDragonEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * Ender Dragon'un maksimum canını 200'den 600'e çıkarır (3 katı can).
     */
    @Inject(method = "createEnderDragonAttributes", at = @At("RETURN"), cancellable = true)
    private static void hardmod$boostDragonHealth(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.setReturnValue(cir.getReturnValue().add(EntityAttributes.GENERIC_MAX_HEALTH, 600.0D));
    }

    /**
     * Ejderha savaşında dinamik minion çağırma, kule kampçılarını cezalandırma
     * ve ölümcül portal aurası mekaniklerini çalıştırır.
     */
    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void hardmod$tickBrutalDragon(CallbackInfo ci) {
        EnderDragonEntity dragon = (EnderDragonEntity) (Object) this;
        BrutalDragonManager.tickDragonFight(dragon);
    }

    /**
     * Kristaller bağlıyken veya uzaktan vurulurken ejderhanın aldığı hasarı %75 azaltır (Zırh kalkanı).
     */
    @Inject(method = "damagePart", at = @At("HEAD"), cancellable = true)
    private void hardmod$shieldDragonDamage(EnderDragonPart part, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        EnderDragonEntity dragon = (EnderDragonEntity) (Object) this;

        // Kristallerden beslenirken hasarı neredeyse tamamen soğurur
        if (dragon.connectedCrystal != null) {
            // Sadece %20 hasar alır
            part.damage(source, amount * 0.20F);
            cir.setReturnValue(true);
        }
    }
}
