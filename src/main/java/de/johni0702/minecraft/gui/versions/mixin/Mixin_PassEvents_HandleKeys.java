package de.johni0702.minecraft.gui.versions.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.johni0702.minecraft.gui.versions.ScreenExt;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(KeyboardHandler.class)
public class Mixin_PassEvents_HandleKeys {
    @ModifyExpressionValue(
            method = "keyPress",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", ordinal = 0),
            //#if MC>=12102
            //#if MC>=12109
            slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=keyPressed event handler"))
            //#elseif MC>=12102
            //$$ slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;keyPressed(III)Z"))
            //#else
            //$$ slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V"))
            //#endif
    )
    private Screen doesScreenPassEvents(Screen screen) {
        if (screen instanceof ScreenExt ext && ext.doesPassEvents()) {
            screen = null;
        }
        return screen;
    }
}
