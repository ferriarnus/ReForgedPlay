package com.replaymod.render.hooks;

// import com.replaymod.render.utils.FlawlessFrames;
// import com.replaymod.render.utils.FlawlessFramesHelper;
import com.replaymod.render.utils.EmbeddiumFlawlessFramesHelper;
import com.replaymod.render.utils.SodiumFlawlessFramesHelper;
import net.minecraft.client.render.WorldRenderer;
// import net.neoforged.fml.loading.FMLLoader;
// import net.neoforged.fml.loading.LoadingModList;

public class ForceChunkLoadingHook {

    private final WorldRenderer hooked;

    public ForceChunkLoadingHook(WorldRenderer renderGlobal) {
        this.hooked = renderGlobal;

        /*if (FlawlessFramesHelper.hasEmbeddium()) {
            FlawlessFramesHelper.setEnabled(true);
        }*/

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

        /*if (FlawlessFramesHelper.hasEmbeddium()) {
            FlawlessFramesHelper.setEnabled(false);
        }*/

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
