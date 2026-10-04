//#if FABRIC>=1
package de.johni0702.minecraft.gui.versions.mixin;

import de.johni0702.minecraft.gui.versions.callbacks.PostRenderScreenCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    //#if MC >= 26.1
    private static final String EXTRACT_GUI = "extractGui";
    //#else
    //$$ private static final String EXTRACT_GUI = "render";
    //#endif

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
    private GuiGraphicsExtractor context;
    //#else
    //$$ private MatrixStack context;
    //#endif

    @ModifyArg(method = "extractGui", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;drawScreen(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
    //#if MC>=12000
    private GuiGraphicsExtractor captureContext(GuiGraphicsExtractor context) {
    //#else
    //$$ private MatrixStack captureContext(MatrixStack context) {
    //#endif
        this.context = context;
        return context;
    }
    //#endif

    @Inject(method = EXTRACT_GUI, at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;drawScreen(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", shift = At.Shift.AFTER))
    private void postRenderScreen(
            //#if MC>=12100
            DeltaTracker tickCounter,
            //#else
            //$$ float partialTicks, long nanoTime,
            //#endif
            boolean renderWorld,
            //#if MC >= 26.1
            boolean resourcesLoaded,
            //#endif
            CallbackInfo ci
    ) {
        //#if MC>=12100
        float partialTicks = tickCounter.getGameTimeDeltaPartialTick(true);
        //#endif
        //#if MC<11600
        //$$ MatrixStack context = new MatrixStack();
        //#endif
        PostRenderScreenCallback.EVENT.invoker().postRenderScreen(context, partialTicks);
    }
}
//#endif
