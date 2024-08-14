package com.replaymod.render.utils;

public class FlawlessFramesHelper {

    public static boolean hasEmbeddium() {
        return FlawlessFrames.hasSodium();
    }

    public static void setEnabled(boolean enabled) {
        FlawlessFrames.setEnabled(enabled);
    }

}
