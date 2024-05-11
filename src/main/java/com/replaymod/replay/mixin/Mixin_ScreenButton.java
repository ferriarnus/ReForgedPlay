package com.replaymod.replay.mixin;

import com.replaymod.replay.ButtonList;
import com.replaymod.replay.ScreenButtonExtension;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(Screen.class)
public class Mixin_ScreenButton implements ScreenButtonExtension {

    @Shadow
    @Final
    protected List<Selectable> selectables;
    @Shadow
    @Final
    protected List<Element> children;
    @Shadow
    @Final
    protected List<Drawable> drawables;

    @Unique
    private List<ClickableWidget> replayButtons;

    @Override
    public List<ClickableWidget> replay_getButtons() {
        // Lazy init to make the list access safe after Screen#init
        if (this.replayButtons == null) {
            this.replayButtons = new ButtonList(this.drawables, this.selectables, this.children);
        }

        return this.replayButtons;
    }
}
