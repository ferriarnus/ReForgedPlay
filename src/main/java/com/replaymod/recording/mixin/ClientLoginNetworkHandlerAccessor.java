package com.replaymod.recording.mixin;

import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
//#endif

@Mixin(ClientHandshakePacketListenerImpl.class)
public interface ClientLoginNetworkHandlerAccessor {
    //#if MC>=11903
    @Accessor
    ServerData getServerData();
    //#endif
}
