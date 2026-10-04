package com.replaymod.extras.advancedscreenshots;

import com.replaymod.core.versions.MCVer;
import com.replaymod.render.RenderSettings;
import com.replaymod.render.blend.BlendState;
import com.replaymod.render.capturer.RenderInfo;
import com.replaymod.render.hooks.ForceChunkLoadingHook;
import com.replaymod.render.rendering.Pipelines;
import de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;

import static com.replaymod.core.versions.MCVer.resizeMainWindow;

import com.mojang.blaze3d.platform.Window;

public class ScreenshotRenderer implements RenderInfo {

    private final Minecraft mc = MCVer.getMinecraft();

    private final RenderSettings settings;

    private int framesDone;

    public ScreenshotRenderer(RenderSettings settings) {
        this.settings = settings;
    }

    public boolean renderScreenshot() throws Throwable {
        try {
            Window window = mc.getWindow();
            int widthBefore = window.getWidth();
            int heightBefore = window.getHeight();

            ForceChunkLoadingHook clrg = new ForceChunkLoadingHook(mc.levelRenderer);

            if (settings.getRenderMethod() == RenderSettings.RenderMethod.BLEND) {
                BlendState.setState(new BlendState(settings.getOutputFile()));
                Pipelines.newBlendPipeline(this).run();
            } else {
                Pipelines.newPipeline(settings.getRenderMethod(), this,
                        new ScreenshotWriter(settings.getOutputFile())).run();
            }

            clrg.uninstall();

            resizeMainWindow(mc, widthBefore, heightBefore);
            return true;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            CrashReport report = CrashReport.forThrowable(e, "Creating Equirectangular Screenshot");
            MCVer.getMinecraft().delayCrash(report);
        }
        return false;
    }

    @Override
    public ReadableDimension getFrameSize() {
        return new Dimension(settings.getVideoWidth(), settings.getVideoHeight());
    }

    @Override
    public int getFramesDone() {
        return framesDone;
    }

    @Override
    public int getTotalFrames() {
        // render 2 frames, because only the second contains all frames fully loaded
        return 2;
    }

    @Override
    public float updateForNextFrame() {
        framesDone++;
        return mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
    }

    @Override
    public RenderSettings getRenderSettings() {
        return settings;
    }
}
