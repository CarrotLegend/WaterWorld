package com.carrot123.waterworld.mixin;

import com.carrot123.waterworld.worldgen.OceanSpawnPlatform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Redirect(method = "placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;load(Lnet/minecraft/server/level/ServerPlayer;)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag waterworld$placeFirstPlayer(PlayerList players, ServerPlayer player) {
        CompoundTag data = players.load(player);
        if (data == null) {
            OceanSpawnPlatform.placeFirstPlayer(player);
        }
        return data;
    }
}
