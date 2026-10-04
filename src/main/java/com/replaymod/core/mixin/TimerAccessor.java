package com.replaymod.core.mixin;

import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

//#if MC>=12100
@Mixin(DeltaTracker.Timer.class)
//#else
//$$ @Mixin(RenderTickCounter.class)
//#endif
public interface TimerAccessor {
    //#if MC>=11200
    @Accessor("msPerTick")
    float getTickLength();
    @Accessor("msPerTick")
    @Mutable
    void setTickLength(float value);
    //#else
    //$$ @Accessor
    //$$ float getTimerSpeed();
    //$$ @Accessor
    //$$ void setTimerSpeed(float value);
    //#endif
}
