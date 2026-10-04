package com.replaymod.replay.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientLevel.class)
public interface ClientWorldAccessor {
    //#if MC>=11800
    @Accessor
    net.minecraft.world.level.entity.EntityTickList getTickingEntities();
    //#endif
}
