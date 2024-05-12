//#if FABRIC>=1
package de.johni0702.minecraft.gui.versions.mixin;

import de.johni0702.minecraft.gui.versions.callbacks.PostRenderScreenCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#else
//$$ import net.minecraft.client.util.math.MatrixStack;
//#endif

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    //#if MC>=12000
    private static final String RENDER = "Lnet/minecraft/client/gui/screen/Screen;renderWithTooltip(Lnet/minecraft/client/gui/DrawContext;IIF)V";
    //#elseif MC>=11903
    //$$ private static final String RENDER = "Lnet/minecraft/client/gui/screen/Screen;renderWithTooltip(Lnet/minecraft/client/util/math/MatrixStack;IIF)V";
    //#elseif MC>=11600
    //$$ private static final String RENDER = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V";
    //#else
    //$$ private static final String RENDER = "Lnet/minecraft/client/gui/screen/Screen;render(IIF)V";
    //#endif

    //#if MC>=11600
    @Unique
    //#if MC>=12000
    private DrawContext context;
    //#else
    //$$ private MatrixStack context;
    //#endif

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/gui/DrawContext;IIF)V"))
    //#if MC>=12000
    private DrawContext captureContext(DrawContext context) {
    //#else
    //$$ private MatrixStack captureContext(MatrixStack context) {
    //#endif
        this.context = context;
        return context;
    }
    //#endif

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/gui/DrawContext;IIF)V", shift = At.Shift.AFTER))
    private void postRenderScreen(float partialTicks, long nanoTime, boolean renderWorld, CallbackInfo ci) {
        //#if MC<11600
        //$$ MatrixStack context = new MatrixStack();
        //#endif
        PostRenderScreenCallback.EVENT.invoker().postRenderScreen(context, partialTicks);
    }
}
//#endif
