//#if MC>=11400
package com.replaymod.core.mixin;

import com.replaymod.core.events.PostRenderWorldCallback;
import com.replaymod.core.events.PreRenderHandCallback;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(
            method = "renderWorld",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z"
            )
    )
    private void postRenderWorld(
            float partialTicks,
            long nanoTime,
            //#if MC>=11500
            //#endif
            CallbackInfo ci) {
        //#if MC<11500
        //$$ MatrixStack matrixStack = new MatrixStack();
        //#endif
        PostRenderWorldCallback.EVENT.invoker().postRenderWorld(new MatrixStack());
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void preRenderHand(
            Camera camera,
            //#if MC>=11500
            float partialTicks,
            Matrix4f matrixStack,
            //#endif
            CallbackInfo ci) {
        if (PreRenderHandCallback.EVENT.invoker().preRenderHand()) {
            ci.cancel();
        }
    }
}
//#endif
