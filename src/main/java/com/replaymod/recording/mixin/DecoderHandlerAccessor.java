package com.replaymod.recording.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nonnull;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;

@Mixin(PacketDecoder.class)
public interface DecoderHandlerAccessor<T extends PacketListener> {
    @Accessor
    @Nonnull
    ProtocolInfo<T> getProtocolInfo();
}