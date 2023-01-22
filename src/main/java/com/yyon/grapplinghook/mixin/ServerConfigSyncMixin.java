package com.yyon.grapplinghook.mixin;

import com.yyon.grapplinghook.common.CommonSetup;
import com.yyon.grapplinghook.config.GrappleConfig;
import com.yyon.grapplinghook.network.LoggedInMessage;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class ServerConfigSyncMixin {

    @Inject(method = "placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("TAIL"))
    public void onLogin(Connection netManager, ServerPlayer player, CallbackInfo ci) {
        CommonSetup.network.send(PacketDistributor.PLAYER.with(() -> player), new LoggedInMessage(GrappleConfig.getConf()));
    }

}
