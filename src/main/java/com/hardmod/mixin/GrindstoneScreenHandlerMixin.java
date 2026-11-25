package com.hardmod.mixin;

import com.hardmod.curse.CurseManager;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrindstoneScreenHandler.class)
public abstract class GrindstoneScreenHandlerMixin extends ScreenHandler {

    @Shadow @Final private Inventory result;
    @Shadow @Final private Inventory input;

    protected GrindstoneScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    /**
     * Bileme taşında sonuç güncellendiğinde:
     * Girdilerde Kaybolma Laneti varsa çıktıda ASLA silinemez!
     * Çıktıya otomatik olarak Kaybolma Laneti yeniden zorunlu kılınır.
     */
    @Inject(method = "updateResult", at = @At("TAIL"))
    private void hardmod$preventCurseRemoval(CallbackInfo ci) {
        ItemStack outputStack = this.result.getStack(0);
        if (!outputStack.isEmpty()) {
            boolean hasAnyInput = false;
            for (int i = 0; i < this.input.size(); i++) {
                if (!this.input.getStack(i).isEmpty()) {
                    hasAnyInput = true;
                    break;
                }
            }

            if (hasAnyInput) {
                CurseManager.ensureCurseOnStack(outputStack);
            }
        }
    }
}
