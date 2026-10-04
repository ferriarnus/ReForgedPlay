package com.replaymod.render.mixin;

import net.minecraft.client.renderer.state.GameRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
//#if MC>=12106
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    //#if MC<12106
    //$$ @Accessor
    //$$ boolean getRenderHand();
    //$$ @Accessor
    //$$ void setRenderHand(boolean value);
    //#endif

    //#if MC>=12106
    @Accessor
    CrossFrameResourcePool getResourcePool();
    @Accessor
    GuiRenderer getGuiRenderer();
    //#if MC >= 26.1
    @Accessor
    GameRenderState getGameRenderState();
    //#else
    //$$ @Accessor
    //$$ net.minecraft.client.gui.render.state.GuiRenderState getGuiState();
    //#endif
    @Accessor
    FogRenderer getFogRenderer();
    //#endif
}
