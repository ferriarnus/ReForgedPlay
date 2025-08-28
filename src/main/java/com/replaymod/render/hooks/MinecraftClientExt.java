package com.replaymod.render.hooks;

import com.replaymod.render.gui.progress.VirtualWindow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

public interface MinecraftClientExt {
    void setWindowDelegate(VirtualWindow window);

    static MinecraftClientExt get(MinecraftClient mc) {
        return (MinecraftClientExt) mc;
    }

    void setFramebufferDelegate(Framebuffer framebuffer);
}
