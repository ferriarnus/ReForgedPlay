//#if MC>=10904 && MC<11900
package com.replaymod.render.blend.mixin;

import net.minecraft.client.renderer.entity.ItemEntityRenderer;
//import net.minecraft.client.color.item.ItemColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemEntityRenderer.class)
public interface ItemRendererAccessor {
    //@Accessor("colors")
    //ItemColors getItemColors();
}
//#endif
