package com.replaymod.core.mixin;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
//#else
//$$ import net.minecraft.client.gui.GuiButton;
//$$ import org.spongepowered.asm.mixin.gen.Accessor;
//$$ import java.util.List;
//#endif

@Mixin(Screen.class)
public interface GuiScreenAccessor {
    //#if MC>=11700
    @Invoker("addRenderableWidget")
    <T extends GuiEventListener & Renderable & NarratableEntry> T invokeAddButton(T drawableElement);
    //#elseif MC>=11400
    //$$ @Invoker
    //$$ <T extends AbstractButtonWidget> T invokeAddButton(T button);
    //#else
    //$$ @Accessor("buttonList")
    //$$ List<GuiButton> getButtons();
    //#endif
}
