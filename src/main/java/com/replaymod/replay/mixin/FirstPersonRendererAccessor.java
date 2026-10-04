package com.replaymod.replay.mixin;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemInHandRenderer.class)
public interface FirstPersonRendererAccessor {
    //#if MC>=10904
    @Accessor("mainHandItem")
    void setItemStackMainHand(ItemStack value);
    @Accessor("offHandItem")
    void setItemStackOffHand(ItemStack value);
    @Accessor("mainHandHeight")
    void setEquippedProgressMainHand(float value);
    @Accessor("oMainHandHeight")
    void setPrevEquippedProgressMainHand(float value);
    @Accessor("offHandHeight")
    void setEquippedProgressOffHand(float value);
    @Accessor("oOffHandHeight")
    void setPrevEquippedProgressOffHand(float value);
    //#else
    //$$ @Accessor
    //$$ void setItemToRender(ItemStack value);
    //$$ @Accessor
    //$$ void setEquippedItemSlot(int value);
    //$$ @Accessor
    //$$ void setEquippedProgress(float value);
    //$$ @Accessor
    //$$ void setPrevEquippedProgress(float value);
    //#endif
}
