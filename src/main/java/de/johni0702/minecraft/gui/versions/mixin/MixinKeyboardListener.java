package de.johni0702.minecraft.gui.versions.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.johni0702.minecraft.gui.function.CharInput;
import de.johni0702.minecraft.gui.function.KeyInput;
import de.johni0702.minecraft.gui.versions.callbacks.KeyboardCallback;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardListener {
    @WrapOperation(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z"))
    private boolean keyPressed(Screen instance, KeyEvent mcKeyInput, Operation<Boolean> original) {
        if (KeyboardCallback.EVENT.invoker().keyPressed(new KeyInput(mcKeyInput))) {
            return true;
        }
        return original.call(instance, mcKeyInput);
    }

    @WrapOperation(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;keyReleased(Lnet/minecraft/client/input/KeyEvent;)Z"))
    private boolean keyReleased(Screen instance, KeyEvent mcKeyInput, Operation<Boolean> original) {
        if (KeyboardCallback.EVENT.invoker().keyReleased(new KeyInput(mcKeyInput))) {
            return true;
        }
        return original.call(instance, mcKeyInput);
    }

    @WrapOperation(method = "charTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;charTyped(Lnet/minecraft/client/input/CharacterEvent;)Z"))
    private boolean charTyped(Screen instance, CharacterEvent mcCharInput, Operation<Boolean> original) {
        if (KeyboardCallback.EVENT.invoker().charTyped(new CharInput(mcCharInput))) {
            return true;
        }
        return original.call(instance, mcCharInput);
    }
}