package com.replaymod.render.gui.progress;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.replaymod.render.hooks.MinecraftClientExt;
import com.replaymod.render.mixin.MainWindowAccessor;
import de.johni0702.minecraft.gui.function.Closeable;
import net.minecraft.client.Minecraft;

public class VirtualWindow implements Closeable {
    private final Minecraft mc;
    private final Window window;
    private final MainWindowAccessor acc;

    private final RenderTarget guiFramebuffer;
    private boolean isBound;
    private int framebufferWidth, framebufferHeight;

    private int gameWidth, gameHeight;


    public VirtualWindow(Minecraft mc) {
        this.mc = mc;
        this.window = mc.getWindow();
        this.acc = (MainWindowAccessor) (Object) this.window;

        framebufferWidth = acc.getFramebufferWidth();
        framebufferHeight = acc.getFramebufferHeight();

        //#if MC>=11700
        guiFramebuffer = new MainTarget(framebufferWidth, framebufferHeight);
        //#else
        //$$ guiFramebuffer = new Framebuffer(framebufferWidth, framebufferHeight, true
                //#if MC>=11400
                //$$ , false
                //#endif
        //$$ );
        //#endif

        MinecraftClientExt.get(mc).setWindowDelegate(this);
    }

    @Override
    public void close() {
        guiFramebuffer.destroyBuffers();

        MinecraftClientExt.get(mc).setWindowDelegate(null);
    }

    public void bind() {
        gameWidth = acc.getFramebufferWidth();
        gameHeight = acc.getFramebufferHeight();
        acc.setFramebufferWidth(framebufferWidth);
        acc.setFramebufferHeight(framebufferHeight);
        applyScaleFactor();
        isBound = true;
    }

    public void unbind() {
        acc.setFramebufferWidth(gameWidth);
        acc.setFramebufferHeight(gameHeight);
        applyScaleFactor();
        isBound = false;
    }

    public void beginWrite() {
        MinecraftClientExt.get(mc).setFramebufferDelegate(guiFramebuffer);
        //#if MC<12105
        //$$ guiFramebuffer.beginWrite(true);
        //#endif
    }

    public void endWrite() {
        //#if MC<12105
        //$$ guiFramebuffer.endWrite();
        //#endif
        MinecraftClientExt.get(mc).setFramebufferDelegate(null);
    }

    public void flip() {
        //#if MC>=12105
        guiFramebuffer.blitToScreen();
        //#else
        //$$ guiFramebuffer.draw(framebufferWidth, framebufferHeight);
        //#endif

        //#if MC>=11500
        //#if MC >= 26.1
        RenderSystem.flipFrame(null);
        //#elseif MC>=12102
        //$$ window.updateDisplay(null);
        //#elseif MC>=11500
        //$$ window.swapBuffers();
        //#else
        //#if MC>=11400
        //$$ window.setFullscreen(false);
        //#else
        //#if MC>=10800
        //$$ mc.updateDisplay();
        //#else
        //$$ mc.resetSize();
        //#endif
        //#endif
        //#endif
    }

    /**
     * Updates the size of the window's framebuffer. Must only be called while this window is bound.
     */
    public void onResolutionChanged(int newWidth, int newHeight) {
        if (newWidth == 0 || newHeight == 0) {
            // These can be zero on Windows if minimized.
            // Creating zero-sized framebuffers however will throw an error, so we never want to switch to zero values.
            return;
        }

        if (framebufferWidth == newWidth && framebufferHeight == newHeight) {
            return; // size is unchanged, nothing to do
        }

        framebufferWidth = newWidth;
        framebufferHeight = newHeight;

        //#if MC>=11400
        guiFramebuffer.resize(newWidth, newHeight
                //#if MC>=11400 && MC<12102
                //$$ , false
                //#endif
        );
        //#else
        //$$ guiFramebuffer.createBindFramebuffer(newWidth, newHeight);
        //#endif

        applyScaleFactor();
        if (mc.screen != null) {
            //#if MC>=12111
            mc.screen.resize(window.getGuiScaledWidth(), window.getGuiScaledHeight());
            //#else
            //$$ mc.screen.resize(mc, window.getGuiScaledWidth(), window.getGuiScaledHeight());
            //#endif
        }
    }

    private void applyScaleFactor() {
        //#if MC>=11400
        window.setGuiScale(window.calculateScale(mc.options.guiScale().get(), mc.isEnforceUnicode()));
        //#else
        //$$ // Nothing to do, ScaledResolution re-computes the scale factor every time it is created
        //#endif
    }

    public int getFramebufferWidth() {
        return framebufferWidth;
    }

    public int getFramebufferHeight() {
        return framebufferHeight;
    }

    public boolean isBound() {
        return isBound;
    }
}
