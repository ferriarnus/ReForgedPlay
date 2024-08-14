package com.replaymod.render.hooks;

import com.replaymod.render.utils.FlawlessFrames;
import com.replaymod.render.utils.FlawlessFramesHelper;
import net.minecraft.client.render.WorldRenderer;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;

public class ForceChunkLoadingHook {

    private final WorldRenderer hooked;

    public ForceChunkLoadingHook(WorldRenderer renderGlobal) {
        this.hooked = renderGlobal;

        if (LoadingModList.get().getModFileById("embeddium") != null) {
            FlawlessFramesHelper.setEnabled(true);
        }
        IForceChunkLoading.from(renderGlobal).replayModRender_setHook(this);
    }

    public void uninstall() {
        IForceChunkLoading.from(hooked).replayModRender_setHook(null);
        if (LoadingModList.get().getModFileById("embeddium") != null) {
            FlawlessFramesHelper.setEnabled(false);
        }
    }

    public interface IBlockOnChunkRebuilds {
        boolean uploadEverythingBlocking();
    }
}
