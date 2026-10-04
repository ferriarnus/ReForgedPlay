package com.replaymod.core.mixin;

import com.replaymod.core.versions.MCVer;
import net.minecraft.network.PacketProcessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC>=11400
import com.replaymod.core.events.PostRenderCallback;
import com.replaymod.core.events.PreRenderCallback;
//#else
//$$ import org.spongepowered.asm.mixin.injection.Redirect;
//$$ import com.replaymod.replay.InputReplayTimer;
//$$ import org.lwjgl.input.Mouse;
//#endif

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft
        //#if MC>=11400
        extends ReentrantBlockableEventLoop<Runnable>
        //#endif
        implements MCVer.MinecraftMethodAccessor {

    //#if MC>=11400
    @Shadow protected abstract void handleKeybinds();

    @Override
    public void replayModProcessKeyBinds() {
        handleKeybinds();
    }

    //#if MC>=12109
    @Shadow @Final
    private PacketProcessor packetProcessor;
    //#endif
    //#if MC>=11400
    @Override
    public void replayModExecuteTaskQueue() {
        //#if MC>=12109
        this.packetProcessor.processQueuedPackets();
        //#endif
        runAllTasks();
    }
    //#endif

    @Inject(method = "runTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;renderFrame(Z)V"))
    private void preRender(boolean unused, CallbackInfo ci) {
        PreRenderCallback.EVENT.invoker().preRender();
    }

    @Inject(method = "runTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;renderFrame(Z)V",
                    shift = At.Shift.AFTER))
    private void postRender(boolean unused, CallbackInfo ci) {
        PostRenderCallback.EVENT.invoker().postRender();
    }
    //#else
    //$$ @Shadow long systemTime;
    //#if MC>=10904
    //$$ @Shadow protected abstract void runTickKeyboard() throws IOException;
    //$$ @Shadow protected abstract void runTickMouse() throws IOException;
    //$$
    //$$ @Override
    //$$ public void replayModRunTickKeyboard() {
    //$$     try {
    //$$         runTickKeyboard();
    //$$     } catch (IOException e) {
    //$$         e.printStackTrace();
    //$$     }
    //$$ }
    //$$
    //$$ @Override
    //$$ public void replayModRunTickMouse() {
    //$$     try {
    //$$         runTickMouse();
    //$$         // Update last tick time (MC ignores inputs when there hasn't been a tick in 200ms)
    //$$         systemTime = Minecraft.getSystemTime();
    //$$     } catch (IOException e) {
    //$$         e.printStackTrace();
    //$$     }
    //$$ }
    //#else
    //$$ private boolean earlyReturn;
    //$$
    //$$ @Override
    //$$ public void replayModSetEarlyReturnFromRunTick(boolean earlyReturn) {
    //$$     this.earlyReturn = earlyReturn;
    //$$ }
    //$$
    //$$ @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;sendClickBlockToController(Z)V"), cancellable = true)
    //$$ private void doEarlyReturnFromRunTick(CallbackInfo ci) {
    //$$     if (earlyReturn) {
    //$$         ci.cancel();
    //$$
    //$$         // Update last tick time (MC ignores inputs when there hasn't been a tick in 200ms)
    //$$         systemTime = Minecraft.getSystemTime();
    //$$     }
    //$$ }
    //#endif
    //$$ @Redirect(
            //#if MC>=10904
            //$$ method = "runTickMouse",
            //#else
            //$$ method = "runTick",
            //#endif
    //$$         at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I", remap = false)
    //$$ )
    //$$ private int scroll() {
    //$$     int wheel = Mouse.getEventDWheel();
    //$$     InputReplayTimer.handleScroll(wheel);
    //$$     return wheel;
    //$$ }
    //#endif

    //#if MC >= 26.1
    MixinMinecraft() { super(null, false); }
    //#elseif MC>=11400
    //$$ MixinMinecraft() { super(null); }
    //#endif
}
