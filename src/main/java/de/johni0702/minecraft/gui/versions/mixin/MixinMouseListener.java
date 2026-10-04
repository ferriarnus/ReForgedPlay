package de.johni0702.minecraft.gui.versions.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import de.johni0702.minecraft.gui.function.Click;
import de.johni0702.minecraft.gui.versions.callbacks.MouseCallback;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MixinMouseListener {
    @WrapOperation(method = "onButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z"))
    private boolean mouseDown(Screen instance, MouseButtonEvent mouseButtonEvent, boolean b, Operation<Boolean> original) {
        if (MouseCallback.EVENT.invoker().mouseDown(new de.johni0702.minecraft.gui.function.Click(mouseButtonEvent))) {
            return true;
        }
        return original.call(instance, mouseButtonEvent, b);
    }

    @WrapOperation(method = "onButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseReleased(Lnet/minecraft/client/input/MouseButtonEvent;)Z"))
    private boolean mouseUp(Screen instance, MouseButtonEvent mouseButtonEvent, Operation<Boolean> original) {
        if (MouseCallback.EVENT.invoker().mouseUp(new de.johni0702.minecraft.gui.function.Click(mouseButtonEvent))) {
            return true;
        }
        return original.call(instance, mouseButtonEvent);
    }

    @WrapOperation(method = "handleAccumulatedMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseDragged(Lnet/minecraft/client/input/MouseButtonEvent;DD)Z"))
    private boolean mouseDrag(Screen instance, MouseButtonEvent mouseButtonEvent, double dx, double dy, Operation<Boolean> original) {
        if (MouseCallback.EVENT.invoker().mouseDrag(new de.johni0702.minecraft.gui.function.Click(mouseButtonEvent), dx, dy)) {
            return true;
        }
        return original.call(instance, mouseButtonEvent, dx, dy);
    }

    @WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDDD)Z"))
    private boolean mouseScroll(Screen screen, double x, double y, double horizontal, double vertical, Operation<Boolean> original) {
        if (MouseCallback.EVENT.invoker().mouseScroll(x, y, horizontal, vertical)) {
            return true;
        }
        return original.call(screen, x, y, horizontal, vertical);
    }
}