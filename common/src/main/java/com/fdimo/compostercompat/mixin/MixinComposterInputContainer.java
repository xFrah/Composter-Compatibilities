package com.fdimo.compostercompat.mixin;

import com.fdimo.compostercompat.CommonClass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.level.block.ComposterBlock$InputContainer")
public abstract class MixinComposterInputContainer {

    @Shadow @Final private BlockState state;
    @Shadow @Final private LevelAccessor level;
    @Shadow @Final private BlockPos pos;

    @Inject(method = "setChanged", at = @At("HEAD"))
    private void onSetChanged(CallbackInfo ci) {
        if (this.level instanceof Level lvl && !lvl.isClientSide()) {
            int fillLevel = this.state.getValue(ComposterBlock.LEVEL);
            if (fillLevel < 7) {
                ItemStack stack = ((SimpleContainer) (Object) this).getItem(0);
                if (!stack.isEmpty()) {
                    CommonClass.CompostData data = CommonClass.COMPOSTER_CACHE.get(stack.getItem());
                    if (data != null && data.probability > 0) {
                        MixinComposterRemainder.spawnCraftingRemainder(lvl, this.pos, data.remainder);
                    }
                }
            }
        }
    }
}

