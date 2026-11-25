package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.fishing.IFishingTreasureCatch;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LootTable.class)
public abstract class FishingTreasureLootMixin {
    @ModifyVariable(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 1, allow = 1)
    private Consumer<ItemStack> waterworld$trackTreasureOutput(Consumer<ItemStack> output, LootContext context,
                                                             Consumer<ItemStack> originalOutput) {
        if (!BuiltInLootTables.FISHING_TREASURE.equals(((LootTable) (Object) this).getLootTableId())
                || !(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof IFishingTreasureCatch fishingCatch)) {
            return output;
        }
        return stack -> {
            if (!stack.isEmpty()) {
                fishingCatch.waterworld$recordTreasure();
            }
            output.accept(stack);
        };
    }
}
