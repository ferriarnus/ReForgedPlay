package com.replaymod.replay.mixin;

import com.replaymod.replay.ButtonList;
import com.replaymod.replay.ScreenButtonExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Screen.class)
public class Mixin_ScreenButton implements ScreenButtonExtension {

    @Shadow
    @Final
    protected List<NarratableEntry> narratables;
    @Shadow
    @Final
    protected List<GuiEventListener> children;
    @Shadow
    @Final
    protected List<Renderable> renderables;

    @Unique
    private List<AbstractWidget> replayButtons;

    @Override
    public List<AbstractWidget> replay_getButtons() {
        // Lazy init to make the list access safe after Screen#init
        if (this.replayButtons == null) {
            this.replayButtons = new ButtonList(this.renderables, this.narratables, this.children);
        }

        return this.replayButtons;
    }
}
