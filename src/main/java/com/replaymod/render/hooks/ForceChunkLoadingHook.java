package com.replaymod.render.hooks;

import com.replaymod.render.utils.EmbeddiumFlawlessFramesHelper;
import com.replaymod.render.utils.SodiumFlawlessFramesHelper;
import net.minecraft.client.renderer.LevelRenderer;

public class ForceChunkLoadingHook {

    private final LevelRenderer hooked;

    public ForceChunkLoadingHook(LevelRenderer renderGlobal) {
        this.hooked = renderGlobal;

        if (EmbeddiumFlawlessFramesHelper.hasEmbeddium()) {
            EmbeddiumFlawlessFramesHelper.setEnabled(true);
        }

        if (SodiumFlawlessFramesHelper.hasSodium()) {
            SodiumFlawlessFramesHelper.setEnabled(true);
        }

        IForceChunkLoading.from(renderGlobal).replayModRender_setHook(this);
    }

    public void uninstall() {
        IForceChunkLoading.from(hooked).replayModRender_setHook(null);

        if (EmbeddiumFlawlessFramesHelper.hasEmbeddium()) {
            EmbeddiumFlawlessFramesHelper.setEnabled(false);
        }

        if (SodiumFlawlessFramesHelper.hasSodium()) {
            SodiumFlawlessFramesHelper.setEnabled(false);
        }
    }

    public interface IBlockOnChunkRebuilds {
        boolean uploadEverythingBlocking();
    }
}
