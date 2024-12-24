package com.replaymod.render.utils;

import net.neoforged.fml.ModList;

public class SodiumFlawlessFramesHelper {

    public static boolean hasSodium() {
        return /*FlawlessFrames.hasSodium()*/ ModList.get().isLoaded("sodium");
    }

    public static boolean supportFlawlessFrames() {
        return SodiumFlawlessFrames.isSupported();
    }

    public static void setEnabled(boolean enabled) {
        SodiumFlawlessFrames.setEnabled(enabled);
    }
}
