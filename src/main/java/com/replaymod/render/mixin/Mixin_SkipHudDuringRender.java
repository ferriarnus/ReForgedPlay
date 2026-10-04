package com.replaymod.render.mixin;

import com.replaymod.render.hooks.EntityRendererHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC>=11400
@Mixin(Gui.class)
//#else
//$$ @Mixin({ GuiIngame.class, net.minecraftforge.client.GuiIngameForge.class })
//#endif
public abstract class Mixin_SkipHudDuringRender {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void replayModRender_skipHudDuringRender(CallbackInfo ci) {
        if (((EntityRendererHandler.IEntityRenderer) Minecraft.getInstance().gameRenderer).replayModRender_getHandler() != null) {
            ci.cancel();
        }
    }
}
