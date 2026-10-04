package com.replaymod.recording.mixin;

import com.replaymod.recording.handler.RecordingEventHandler;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class MixinRenderGlobal implements RecordingEventHandler.RecordingEventSender {

    private RecordingEventHandler recordingEventHandler;

    @Override
    public void setRecordingEventHandler(RecordingEventHandler recordingEventHandler) {
        this.recordingEventHandler = recordingEventHandler;
    }

    @Override
    public RecordingEventHandler getRecordingEventHandler() {
        return recordingEventHandler;
    }

    //#if MC>=10800
    @Inject(method = "destroyBlockProgress", at = @At("HEAD"))
    //#else
    //$$ @Inject(method = "destroyBlockPartially", at = @At("HEAD"))
    //#endif
    public void saveBlockBreakProgressPacket(int breakerId,
                                             //#if MC>=10800
                                             BlockPos pos,
                                             //#else
                                             //$$ int x, int y, int z,
                                             //#endif
                                             int progress, CallbackInfo info) {
        if (recordingEventHandler != null) {
            recordingEventHandler.onBlockBreakAnim(breakerId,
                    //#if MC>=10800
                    pos,
                    //#else
                    //$$ x, y, z,
                    //#endif
                    progress);
        }
    }
}
