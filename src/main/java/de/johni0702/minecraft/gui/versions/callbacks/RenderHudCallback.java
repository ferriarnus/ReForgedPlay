package de.johni0702.minecraft.gui.versions.callbacks;

import de.johni0702.minecraft.gui.utils.Event;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface RenderHudCallback {
    Event<RenderHudCallback> EVENT = Event.create((listeners) ->
            (stack, partialTicks) -> {
                for (RenderHudCallback listener : listeners) {
                    listener.renderHud(stack, partialTicks);
                }
            }
    );

    //#if MC>=12000
    void renderHud(GuiGraphicsExtractor context, float partialTicks);
    //#else
    //$$ void renderHud(MatrixStack stack, float partialTicks);
    //#endif
}
