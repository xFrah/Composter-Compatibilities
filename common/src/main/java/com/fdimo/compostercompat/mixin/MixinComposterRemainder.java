package com.fdimo.compostercompat.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComposterBlock.class)
public class MixinComposterRemainder {

    @Inject(method = "addItem", at = @At("HEAD"))
    private static void onAddItem(Entity entity, BlockState state, net.minecraft.world.level.LevelAccessor levelAccessor, BlockPos pos, ItemStack stack, CallbackInfoReturnable<BlockState> cir) {
        if (levelAccessor instanceof Level level && !level.isClientSide()) {
            int i = state.getValue(ComposterBlock.LEVEL);
            if (i < 7) {
                com.fdimo.compostercompat.CommonClass.CompostData data = com.fdimo.compostercompat.CommonClass.COMPOSTER_CACHE.get(stack.getItem());
                if (data != null && data.probability > 0) {
                    java.util.List<String> tags = com.fdimo.compostercompat.CommonClass.crossVersionGetTags(stack);
                    if (com.fdimo.compostercompat.CommonClass.DEBUG) {
                        com.fdimo.compostercompat.CommonClass.debugLog("Composter used with item {}. Tags: {}. Reason: {}. Remainder: {}", stack.getHoverName().getString(), tags, data.reason, data.remainder);
                    }
                    spawnCraftingRemainder(level, pos, data.remainder);
                } else if (com.fdimo.compostercompat.CommonClass.DEBUG) {
                    com.fdimo.compostercompat.CommonClass.debugLog("Composter used with item {}, but it is not in the cache or probability is 0. Cache has item: {}", stack.getHoverName().getString(), com.fdimo.compostercompat.CommonClass.COMPOSTER_CACHE.containsKey(stack.getItem()));
                }
            }
        }
    }

    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void onUseItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hitResult, CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        if (!level.isClientSide() && !stack.isEmpty()) {
            if (!ComposterBlock.COMPOSTABLES.containsKey(stack.getItem())) {
                java.util.List<String> tags = com.fdimo.compostercompat.CommonClass.crossVersionGetTags(stack);
                if (com.fdimo.compostercompat.CommonClass.DEBUG) {
                    com.fdimo.compostercompat.CommonClass.debugLog("Composter interacted with rejected item {}. Tags: {}", stack.getHoverName().getString(), tags);
                }
            }
        }
    }

    private static void spawnCraftingRemainder(Level level, BlockPos pos, Item remainderItem) {
        if (remainderItem != null) {
            com.fdimo.compostercompat.CommonClass.debugLog("Spawning remainder item: {}", remainderItem);
            Vec3 vec3 = Vec3.atLowerCornerWithOffset(pos, 0.5D, 1.01D, 0.5D).offsetRandom(level.getRandom(), 0.7F);
            ItemEntity itemEntity = new ItemEntity(level, vec3.x(), vec3.y(), vec3.z(), new ItemStack(remainderItem));
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }
}
