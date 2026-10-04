//#if FABRIC>=1
package de.johni0702.minecraft.gui.versions.mixin;

import com.google.common.collect.Collections2;
import de.johni0702.minecraft.gui.versions.callbacks.InitScreenCallback;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
//#endif
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

// Increased priority so we can consider existing third-party buttons when choosing the position for our button
@Mixin(value = Screen.class, priority = 1100)
public class MixinScreen {

    //#if MC>=11700
    @Shadow @Final private List<GuiEventListener> children;
    //#else
    //$$ @Shadow
    //$$ protected @Final List<AbstractButtonWidget> buttons;
    //#endif

    //#if MC>=12111
    @Inject(method = "init(II)V", at = @At("HEAD"))
    //#else
    //$$ @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("HEAD"))
    private void preInit(CallbackInfo ci) {
        firePreInit();
    }

    //#if MC>=12111
    @Inject(method = "init(II)V", at = @At("TAIL"))
    //#else
    //$$ @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        firePostInit();
    }

    //#if MC>=11904
    @Inject(method = "resize", at = @At("HEAD"))
    private void preResize(CallbackInfo ci) {
        firePreInit();
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void resize(CallbackInfo ci) {
        firePostInit();
    }
    //#endif

    @Unique
    private void firePreInit() {
        InitScreenCallback.Pre.EVENT.invoker().preInitScreen((Screen) (Object) this);
    }

    @Unique
    private void firePostInit() {
        InitScreenCallback.EVENT.invoker().initScreen(
                (Screen) (Object) this,
                //#if MC>=11700
                Collections2.transform(Collections2.filter(this.children, it -> it instanceof AbstractWidget), it -> (AbstractWidget) it)
                //#else
                //$$ buttons
                //#endif
        );
    }
}
//#endif
