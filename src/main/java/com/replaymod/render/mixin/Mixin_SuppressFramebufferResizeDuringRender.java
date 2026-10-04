package com.replaymod.render.mixin;

import com.mojang.blaze3d.platform.Window;
import com.replaymod.render.gui.progress.VirtualWindow;
import com.replaymod.render.hooks.MinecraftClientExt;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class Mixin_SuppressFramebufferResizeDuringRender implements MinecraftClientExt {

    @Unique
    private VirtualWindow windowDelegate;

    @Override
    public void setWindowDelegate(VirtualWindow window) {
        this.windowDelegate = window;
    }

    //#if MC>=11400
    @Inject(method = "resizeDisplay", at = @At("HEAD"), cancellable = true)
    //#else
    //$$ @Inject(method = "resize", at = @At("HEAD"), cancellable = true)
    //#endif
    private void suppressResizeDuringRender(CallbackInfo ci) {
        VirtualWindow delegate = this.windowDelegate;
        if (delegate != null && delegate.isBound()) {
            Window window = ((Minecraft) (Object) this).getWindow();
            delegate.onResolutionChanged(window.getWidth(), window.getHeight());
            ci.cancel();
        }
    }
}
