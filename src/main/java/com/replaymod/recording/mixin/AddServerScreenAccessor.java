package com.replaymod.recording.mixin;

import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ManageServerScreen.class)
public interface AddServerScreenAccessor {
    @Accessor
    ServerData getServerData();
}
