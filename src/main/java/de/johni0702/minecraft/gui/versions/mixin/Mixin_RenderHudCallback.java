//#if FABRIC>=1
package de.johni0702.minecraft.gui.versions.mixin;

import de.johni0702.minecraft.gui.versions.callbacks.RenderHudCallback;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraftforge.client.gui.ForgeIngameGui;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#else
import net.minecraft.client.util.math.MatrixStack;
//#endif

@Mixin(ForgeIngameGui.class)
public class Mixin_RenderHudCallback {
    @Inject(
            method = "lambda$static$17", at = @At("HEAD"), remap = false
            //#if MC>=12002
            //$$ at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;shouldShowDebugHud()Z")
            //#else
            //$$ at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/option/GameOptions;debugEnabled:Z")
            //#endif
    )
    //#if MC>=12000
    private static void renderOverlay(ForgeIngameGui gui, MatrixStack stack, float partialTicks, int screenWidth, int screenHeight, CallbackInfo ci) {
    //#elseif MC>=11600
    //$$ private void renderOverlay(MatrixStack stack, float partialTicks, CallbackInfo ci) {
    //#else
    //$$ private void renderOverlay(float partialTicks, CallbackInfo ci) {
    //$$     MatrixStack stack = new MatrixStack();
    //#endif
        RenderHudCallback.EVENT.invoker().renderHud(stack, partialTicks);
    }
}
//#endif
