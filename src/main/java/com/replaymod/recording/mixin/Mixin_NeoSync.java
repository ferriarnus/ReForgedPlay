package com.replaymod.recording.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ServerConfigurationPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.configuration.SyncRegistries;
import net.neoforged.neoforge.network.payload.FrozenRegistryPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletedPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncStartPayload;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;

@Mixin(ServerConfigurationNetworkHandler.class)
public abstract class Mixin_NeoSync extends ServerCommonNetworkHandler implements ServerConfigurationPacketListener, TickablePacketListener {

    @Final
    @Shadow
    private Queue<ServerPlayerConfigurationTask> tasks;

    public Mixin_NeoSync(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Inject(method = "runConfiguration", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/network/ConfigurationInitialization;configureEarlyTasks(Lnet/minecraft/network/listener/ServerConfigurationPacketListener;Ljava/util/function/Consumer;)V"))
    private void injectSync(CallbackInfo ci) {
        if (this.hasChannel(FrozenRegistrySyncStartPayload.TYPE) &&
                this.hasChannel(FrozenRegistryPayload.TYPE) &&
                this.hasChannel(FrozenRegistrySyncCompletedPayload.TYPE) &&
                this.getConnection().isLocal()) {
            this.tasks.add(new SyncRegistries());
        }
    }
}
