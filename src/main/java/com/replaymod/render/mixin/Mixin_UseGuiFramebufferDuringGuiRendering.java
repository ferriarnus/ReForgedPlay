package com.replaymod.render.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.replaymod.render.hooks.MinecraftClientExt;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class Mixin_UseGuiFramebufferDuringGuiRendering implements MinecraftClientExt {

    @Unique
    private RenderTarget framebufferDelegate;

    @Override
    public void setFramebufferDelegate(RenderTarget framebuffer) {
        this.framebufferDelegate = framebuffer;
    }

    @Inject(method = "getMainRenderTarget", at = @At("HEAD"), cancellable = true)
    private void useGuiFramebuffer(CallbackInfoReturnable<RenderTarget> ci) {
        RenderTarget delegate = this.framebufferDelegate;
        if (delegate != null) {
            ci.setReturnValue(delegate);
        }
    }
}