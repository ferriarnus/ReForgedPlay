package com.replaymod.replay.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraftforge.network.HandshakeHandler;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

@Mixin(HandshakeHandler.class)
public abstract class Mixin_HandshakeHandler {
    @Shadow
    private static HandshakeHandler getHandshake(Supplier<NetworkEvent.Context> contextSupplier) {
        return null;
    }

    @Shadow
    static void registerHandshake(ClientConnection manager, NetworkDirection direction) {}

    @Redirect(method = "lambda$biConsumerFor$1", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/network/HandshakeHandler;getHandshake(Ljava/util/function/Supplier;)Lnet/minecraftforge/network/HandshakeHandler;"), remap = false)
    private static HandshakeHandler redirectGetHandshake(Supplier<NetworkEvent.Context> contextSupplier) {
        HandshakeHandler result = getHandshake(contextSupplier);
        if (result == null) {
            registerHandshake(contextSupplier.get().getNetworkManager(), contextSupplier.get().getDirection());
            return getHandshake(contextSupplier);
        }
        return result;
    }
}
