package com.replaymod.render.utils;

import net.neoforged.fml.ModList;

public class EmbeddiumFlawlessFramesHelper {

    public static boolean hasEmbeddium() {
        return ModList.get().isLoaded("embeddium");
    }

    public static boolean supportFlawlessFrames() {
        return EmbeddiumFlawlessFrames.isSupported();
    }

    public static void setEnabled(boolean enabled) {
        EmbeddiumFlawlessFrames.setEnabled(enabled);
    }
}
