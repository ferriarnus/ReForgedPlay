package com.replaymod.recording.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.replaymod.core.versions.MCVer;
import com.replaymod.recording.ReplayModRecording;
import com.replaymod.recording.packet.PacketListener;
import com.replaymod.replaystudio.lib.viaversion.api.protocol.packet.State;
import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.protocol.packets.PacketEnabledPacksData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public abstract class MixinNetHandlerConfigClient {
    @Inject(method = "handleConfigurationFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V"))
    public void recordEnabledPackData(CallbackInfo ci, @Local RegistryAccess.Frozen registryManager) {
        PacketListener packetListener = ReplayModRecording.instance.getConnectionEventHandler().getPacketListener();
        if (packetListener == null) return;

        ByteBuf byteBuf = Unpooled.buffer();
        FriendlyByteBuf buf = new FriendlyByteBuf(byteBuf);
        buf.writeUtf(PacketEnabledPacksData.ID);
        RegistryOps<Tag> ops = registryManager.createSerializationContext(NbtOps.INSTANCE);
        buf.writeVarInt(1);
        write(buf, registryManager.lookupOrThrow(Registries.DIMENSION_TYPE), DimensionType.DIRECT_CODEC, ops);

        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        byteBuf.release();

        int packetIdCustomPayload = getPacketId(new ClientboundCustomPayloadPacket(new BrandPayload("")));
        int packetIdFinish = getPacketId(ClientboundFinishConfigurationPacket.INSTANCE);

        PacketTypeRegistry registry = MCVer.getPacketTypeRegistry(State.CONFIGURATION);
        packetListener.save(new Packet(registry, packetIdCustomPayload, PacketType.ConfigCustomPayload, com.github.steveice10.netty.buffer.Unpooled.wrappedBuffer(bytes)));
        packetListener.save(new Packet(registry, packetIdFinish, PacketType.ConfigFinish, com.github.steveice10.netty.buffer.Unpooled.buffer()));
    }

    @Unique
    private <T> void write(FriendlyByteBuf buf, Registry<T> registry, Codec<T> codec, RegistryOps<Tag> ops) {
        buf.writeUtf(registry.key().identifier().toString());
        buf.writeVarInt(registry.size());
        for (Map.Entry<ResourceKey<T>, T> entry : registry.entrySet()) {
            buf.writeUtf(entry.getKey().identifier().toString());
            buf.writeNbt(codec.encodeStart(ops, entry.getValue()).getOrThrow());
        }
    }

    @Unique
    private int getPacketId(net.minecraft.network.protocol.Packet<? super ClientConfigurationPacketListener> packet) {
        ByteBuf byteBuf = Unpooled.buffer();
        try {
            //#if MC>=12106
            ConfigurationProtocols.CLIENTBOUND.codec().encode(byteBuf, packet);
            //#else
            //$$ ConfigurationStates.S2C.codec().encode(byteBuf, packet);
            //#endif
            return new FriendlyByteBuf(byteBuf).readVarInt();
        } finally {
            byteBuf.release();
        }
    }
}