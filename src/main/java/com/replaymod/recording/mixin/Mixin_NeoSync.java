package com.replaymod.recording.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
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

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class Mixin_NeoSync extends ServerCommonPacketListenerImpl implements ServerConfigurationPacketListener, TickablePacketListener {

    @Final
    @Shadow
    private Queue<ConfigurationTask> configurationTasks;

    public Mixin_NeoSync(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
        super(server, connection, clientData);
    }

    @Inject(method = "runConfiguration", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/network/ConfigurationInitialization;configureEarlyTasks(Lnet/minecraft/network/listener/ServerConfigurationPacketListener;Ljava/util/function/Consumer;)V"))
    private void injectSync(CallbackInfo ci) {
        if (this.hasChannel(FrozenRegistrySyncStartPayload.TYPE) &&
                this.hasChannel(FrozenRegistryPayload.TYPE) &&
                this.hasChannel(FrozenRegistrySyncCompletedPayload.TYPE) &&
                this.getConnection().isMemoryConnection()) {
            this.configurationTasks.add(new SyncRegistries());
        }
    }
}
