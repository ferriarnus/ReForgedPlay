package com.replaymod.replay.mixin;

import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.camera.CameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinPlayerControllerMP {

    @Shadow
    private Minecraft minecraft;

    //#if MC>=10800
    //#if MC>=11400
    @Inject(method = "isAlwaysFlying", at=@At("HEAD"), cancellable = true)
    //#else
    //$$ @Inject(method = "isSpectator", at=@At("HEAD"), cancellable = true)
    //#endif
    private void replayModReplay_isSpectator(CallbackInfoReturnable<Boolean> ci) {
        if (this.minecraft.player instanceof CameraEntity) { // this check should in theory not be required
            ci.setReturnValue(this.minecraft.player.isSpectator());
        }
    }
    //#endif

    //#if MC<=10710
    //$$ @Shadow
    //$$ private NetHandlerPlayClient netClientHandler;
    //$$
    //$$ // Prevent the disconnect GUI from being opened during the short time when the replay is restarted
    //$$ // at which the old network manager is closed but still getting ticked (hence the disconnect GUI opening).
    //$$ @Inject(method = "updateController", at = @At("HEAD"), cancellable = true)
    //$$ private void replayModReplay_onlyTickNeverDisconnect(CallbackInfo ci) {
    //$$     if (ReplayModReplay.instance.getReplayHandler() != null) {
    //$$         if (netClientHandler.getNetworkManager().isChannelOpen()) {
    //$$             netClientHandler.getNetworkManager().processReceivedPackets();
    //$$         }
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //$$
    //$$ @Inject(method = "resetBlockRemoving", at = @At("HEAD"), cancellable = true)
    //$$ private void replayModReplay_skipWorldTick(CallbackInfo ci) {
    //$$     if (this.mc.theWorld == null) {
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //#endif
}
