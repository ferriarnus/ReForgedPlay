package com.replaymod.core.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface KeyBindingAccessor {
    @Accessor("clickCount")
    int getPressTime();
    @Accessor("clickCount")
    void setPressTime(int value);
}
