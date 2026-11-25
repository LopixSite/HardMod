package com.hardmod.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndermanEntity.class)
public abstract class EndermanEntityMixin extends HostileEntity {

    protected EndermanEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * Enderman'ın gözüne odaklanma şartını kaldırır:
     * Oyuncunun görüş alanına girdiği ve görülebildiği anda Enderman doğrudan saldırıya geçer.
     */
    @Inject(method = "isPlayerStaring", at = @At("HEAD"), cancellable = true)
    private void hardmod$aggroOnSight(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        // Görüş hattı (Line of Sight) kontrolü
        if (player.canSee(this)) {
            Vec3d playerLook = player.getRotationVec(1.0F).normalize();
            Vec3d toEnderman = this.getEyePos().subtract(player.getEyePos()).normalize();
            double dot = playerLook.dotProduct(toEnderman);

            // Oyuncunun ön ekran alanında (yaklaşık 70 derece açı) bulunuyorsa
            if (dot > 0.3) {
                cir.setReturnValue(true);
            }
        }
    }
}
