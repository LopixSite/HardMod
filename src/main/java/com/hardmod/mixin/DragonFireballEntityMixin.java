package com.hardmod.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonFireballEntity.class)
public abstract class DragonFireballEntityMixin {

    /**
     * Ejderha ateş topu yere veya oyuncuya çarptığında çarpışma noktasına şimşek düşürür.
     */
    @Inject(method = "onCollision", at = @At("TAIL"))
    private void hardmod$onFireballImpact(HitResult hitResult, CallbackInfo ci) {
        DragonFireballEntity fireball = (DragonFireballEntity) (Object) this;

        if (!fireball.getWorld().isClient() && fireball.getWorld() instanceof ServerWorld world) {
            EntityType.LIGHTNING_BOLT.spawn(world, fireball.getBlockPos(), SpawnReason.TRIGGERED);
        }
    }
}
