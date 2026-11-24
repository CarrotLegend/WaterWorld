package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.oxygen.ChestplateOxygenInventory;
import java.util.function.Consumer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class DivingChestplateBreakMixin {
    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", remap = false),
            require = 1, allow = 1)
    private <T extends LivingEntity> void waterworld$recoverTanks(int amount, T wearer, Consumer<T> onBreak, CallbackInfo ci) {
        ItemStack chestplate = (ItemStack) (Object) this;
        if (!wearer.level().isClientSide && ChestplateOxygenInventory.isChestplate(chestplate)
                && ChestplateOxygenInventory.hasStoredTanks(chestplate)) {
            for (ItemStack tank : ChestplateOxygenInventory.takeAll(chestplate)) {
                if (!tank.isEmpty()) {
                    wearer.spawnAtLocation(tank);
                }
            }
        }
    }
}
