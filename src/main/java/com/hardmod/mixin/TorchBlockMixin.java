package com.hardmod.mixin;

import com.hardmod.torch.TorchBurnoutManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.TorchBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TorchBlock.class)
public abstract class TorchBlockMixin {

    @Inject(method = "onBlockAdded", at = @At("TAIL"))
    private void hardmod$onTorchPlaced(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
        if (!world.isClient()) {
            TorchBurnoutManager.registerTorch(pos, world.getTime());
        }
    }

    @Inject(method = "onStateReplaced", at = @At("HEAD"))
    private void hardmod$onTorchRemoved(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
        if (!world.isClient() && !state.isOf(newState.getBlock())) {
            TorchBurnoutManager.removeTorch(pos);
        }
    }
}
