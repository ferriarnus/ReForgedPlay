package com.replaymod.render.mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.textures.GpuTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//$$ @Mixin(GlResourceManager.class)
//#if MC >= 26.1
@Mixin(targets = "com.mojang.blaze3d.opengl.GlCommandEncoder")
//#else
//$$ @Mixin(net.minecraft.client.gl.GlResourceManager.class)
//#endif
public class Mixin_FixCopyTextureToBuffer_DepthTexture {
    @WrapOperation(
            //#if MC>=12111
            method = "copyTextureToBuffer(Lcom/mojang/blaze3d/textures/GpuTexture;Lcom/mojang/blaze3d/buffers/GpuBuffer;JLjava/lang/Runnable;IIIII)V",
            //#else
            //$$ method = "copyTextureToBuffer(Lcom/mojang/blaze3d/textures/GpuTexture;Lcom/mojang/blaze3d/buffers/GpuBuffer;ILjava/lang/Runnable;IIIII)V",
            //#endif
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/DirectStateAccess;bindFrameBufferTextures(IIIII)V")
    )
    private void setupFramebuffer(
            DirectStateAccess instance,
            int framebuffer, int colorAttachment, int depthAttachment, int mipLevel, int bindTarget,
            Operation<Void> original,
            @Local(argsOnly = true) GpuTexture texture
    ) {
        if (depthAttachment == 0 && texture.getFormat().hasDepthAspect()) {
            depthAttachment = colorAttachment;
            colorAttachment = 0;
        }
        original.call(instance, framebuffer, colorAttachment, depthAttachment, mipLevel, bindTarget);
    }
}