package com.replaymod.core.mixin;

import com.replaymod.core.versions.MCVer;
import com.replaymod.recording.packet.PacketListener;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.s2c.config.ReadyS2CPacket;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.registry.DynamicRegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientConfigurationNetworkHandler.class)
public class PlayStateGetterMixin {

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;transitionInbound(Lnet/minecraft/network/NetworkState;Lnet/minecraft/network/listener/PacketListener;)V"), method = "onReady")
    private NetworkState playGetter(NetworkState state) {
        MCVer.play = state;
        return state;
    }
}
