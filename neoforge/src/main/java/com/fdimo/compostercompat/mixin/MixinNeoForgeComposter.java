package com.fdimo.compostercompat.mixin;

import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComposterBlock.class)
public class MixinNeoForgeComposter {
    @Inject(method = "getValue", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private static void overrideComposterValue(ItemStack stack, CallbackInfoReturnable<Float> cir) {
        if (cir.getReturnValue() > 0.0f) {
            // Check if the item has been nullified by c:not_compostable tag
            com.fdimo.compostercompat.CommonClass.CompostData data = com.fdimo.compostercompat.CommonClass.COMPOSTER_CACHE.get(stack.getItem());
            if (data != null && data.reason != null && data.reason.equals("Tag: c:not_compostable")) {
                cir.setReturnValue(0.0f);
                return;
            }
            if (com.fdimo.compostercompat.CommonClass.DEBUG) {
                com.fdimo.compostercompat.CommonClass.debugLog("NeoForge Composter recognized item {} natively with chance {}", stack.getHoverName().getString(), cir.getReturnValue());
            }
        } else {
            com.fdimo.compostercompat.CommonClass.CompostData data = com.fdimo.compostercompat.CommonClass.COMPOSTER_CACHE.get(stack.getItem());
            if (data != null && data.probability > 0) {
                if (com.fdimo.compostercompat.CommonClass.DEBUG) {
                    com.fdimo.compostercompat.CommonClass.debugLog("NeoForge Composter overriding item {} via Cache with chance {}", stack.getHoverName().getString(), data.probability);
                }
                cir.setReturnValue(data.probability);
            }
        }
    }
}
