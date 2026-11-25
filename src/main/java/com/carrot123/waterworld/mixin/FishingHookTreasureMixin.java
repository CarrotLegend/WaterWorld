package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.fishing.IFishingTreasureCatch;
import com.carrot123.waterworld.registry.ModSounds;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class FishingHookTreasureMixin implements IFishingTreasureCatch {
    @Unique
    private boolean waterworld$generatingFishingLoot;
    @Unique
    private boolean waterworld$generatedTreasure;
    @Unique
    private boolean waterworld$successfulTreasureCatch;

    @Inject(method = "retrieve(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), require = 1, allow = 1)
    private void waterworld$resetTreasureCatch(ItemStack rod, CallbackInfoReturnable<Integer> cir) {
        waterworld$generatingFishingLoot = false;
        waterworld$generatedTreasure = false;
        waterworld$successfulTreasureCatch = false;
    }

    @Redirect(method = "retrieve(Lnet/minecraft/world/item/ItemStack;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"),
            require = 1, allow = 1)
    private ObjectArrayList<ItemStack> waterworld$generateFishingLoot(LootTable table, LootParams params) {
        waterworld$generatingFishingLoot = true;
        try {
            ObjectArrayList<ItemStack> drops = table.getRandomItems(params);
            waterworld$generatedTreasure &= drops.stream().anyMatch(stack -> !stack.isEmpty());
            return drops;
        } finally {
            waterworld$generatingFishingLoot = false;
        }
    }

    @Override
    public void waterworld$recordTreasure() {
        if (waterworld$generatingFishingLoot) {
            waterworld$generatedTreasure = true;
        }
    }

    @Inject(method = "retrieve(Lnet/minecraft/world/item/ItemStack;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/FishingRodHookedTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/projectile/FishingHook;Ljava/util/Collection;)V", ordinal = 1),
            require = 1, allow = 1)
    private void waterworld$acceptTreasureCatch(ItemStack rod, CallbackInfoReturnable<Integer> cir) {
        waterworld$successfulTreasureCatch = waterworld$generatedTreasure;
    }

    @Inject(method = "retrieve(Lnet/minecraft/world/item/ItemStack;)I", at = @At("RETURN"), require = 1)
    private void waterworld$playTreasureSound(ItemStack rod, CallbackInfoReturnable<Integer> cir) {
        boolean playSound = waterworld$successfulTreasureCatch;
        waterworld$generatingFishingLoot = false;
        waterworld$generatedTreasure = false;
        waterworld$successfulTreasureCatch = false;
        FishingHook hook = (FishingHook) (Object) this;
        if (playSound && !hook.level().isClientSide && hook.getPlayerOwner() instanceof ServerPlayer player) {
            player.playNotifySound(ModSounds.FISHING_TREASURE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
}
