package com.hardmod.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystalEntity.class)
public abstract class EndCrystalEntityMixin {

    /**
     * Kristal patlatıldığında 8.0 güçte devasa bir şok patlaması yaratır.
     */
    @Inject(method = "damage", at = @At("HEAD"))
    private void hardmod$boostCrystalExplosion(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        EndCrystalEntity crystal = (EndCrystalEntity) (Object) this;

        if (!crystal.getWorld().isClient() && crystal.getWorld() instanceof ServerWorld world && !crystal.isRemoved()) {
            world.createExplosion(
                null,
                crystal.getDamageSources().explosion(crystal, source.getAttacker()),
                null,
                crystal.getX(),
                crystal.getY(),
                crystal.getZ(),
                8.0F,
                false,
                World.ExplosionSourceType.BLOCK
            );
        }
    }
}
