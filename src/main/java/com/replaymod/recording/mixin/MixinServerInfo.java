package com.replaymod.recording.mixin;

import com.replaymod.recording.ServerInfoExt;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerInfo.class)
public abstract class MixinServerInfo implements ServerInfoExt {
    private Boolean autoRecording;

    @Override
    public Boolean getAutoRecording() {
        return autoRecording;
    }

    @Override
    public void setAutoRecording(Boolean autoRecording) {
        this.autoRecording = autoRecording;
    }

    @Inject(method = "toNbt", at = @At("RETURN"))
    private void serialize(CallbackInfoReturnable<NbtCompound> ci) {
        NbtCompound tag = ci.getReturnValue();
        if (autoRecording != null) {
            tag.putBoolean("autoRecording", autoRecording);
        }
    }

    @Inject(method = "fromNbt", at = @At("RETURN"))
    private static void deserialize(NbtCompound tag, CallbackInfoReturnable<ServerInfo> ci) {
        ServerInfoExt serverInfo = ServerInfoExt.from(ci.getReturnValue());
        if (tag.contains("autoRecording")) {
            serverInfo.setAutoRecording(tag.getBoolean("autoRecording"));
        }
    }

    @Inject(method = "copyWithSettingsFrom", at = @At("RETURN"))
    public void copyFrom(ServerInfo serverInfo, CallbackInfo ci) {
        ServerInfoExt from = ServerInfoExt.from(serverInfo);
        this.autoRecording = from.getAutoRecording();
    }
}
