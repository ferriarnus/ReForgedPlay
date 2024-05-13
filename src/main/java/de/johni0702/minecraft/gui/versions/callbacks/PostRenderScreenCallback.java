package de.johni0702.minecraft.gui.versions.callbacks;

import de.johni0702.minecraft.gui.utils.Event;
import net.minecraft.client.util.math.MatrixStack;
//#else
//$$ import net.minecraft.client.util.math.MatrixStack;
//#endif

public interface PostRenderScreenCallback {
    Event<PostRenderScreenCallback> EVENT = Event.create((listeners) ->
            (stack, partialTicks) -> {
                for (PostRenderScreenCallback listener : listeners) {
                    listener.postRenderScreen(stack, partialTicks);
                }
            }
    );

    //#if MC>=12000
    //$$ void postRenderScreen(DrawContext context, float partialTicks);
    //#else
    void postRenderScreen(MatrixStack stack, float partialTicks);
    //#endif
}
