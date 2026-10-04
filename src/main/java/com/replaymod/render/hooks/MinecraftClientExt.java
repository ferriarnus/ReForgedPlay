package com.replaymod.render.hooks;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.replaymod.render.gui.progress.VirtualWindow;
import net.minecraft.client.Minecraft;

public interface MinecraftClientExt {
    void setWindowDelegate(VirtualWindow window);

    static MinecraftClientExt get(Minecraft mc) {
        return (MinecraftClientExt) mc;
    }

    void setFramebufferDelegate(RenderTarget framebuffer);
}
