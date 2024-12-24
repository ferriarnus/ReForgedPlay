package com.replaymod.render.utils;

import org.embeddedt.embeddium.api.service.FlawlessFramesService;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.replaymod.core.ReplayMod.MOD_ID;

/**
 * Uses the "Flawless Frames" FREX feature which allows us to instruct third-party mods to sacrifice performance (even
 * beyond the point where it can no longer achieve interactive frame rates) in exchange for a noticeable boost to
 * quality.
 * In particular also to force-load all chunks with Canvas/Sodium/Bobby.
 *
 * See https://github.com/grondag/frex/pull/9
 */
public class /*lawlessFrames*/ EmbeddiumFlawlessFrames implements FlawlessFramesService {
    private static final List<Consumer<Boolean>> CONSUMERS = new CopyOnWriteArrayList<>();
    private static boolean supported;

    public EmbeddiumFlawlessFrames() {}

    public void acceptController(Function<String, Consumer<Boolean>> controller) {
        Consumer<Boolean> consumer = controller.apply(MOD_ID);
        CONSUMERS.add(consumer);

        if (controller.getClass().getName().contains(".embeddium.")) {
            supported = true;
        }
    }

    public static void setEnabled(boolean enabled) {
        CONSUMERS.forEach(it -> it.accept(enabled));
    }

    public static boolean isSupported() {
        return supported;
    }
}
