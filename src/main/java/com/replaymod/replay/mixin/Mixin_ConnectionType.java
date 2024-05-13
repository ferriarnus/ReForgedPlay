package com.replaymod.replay.mixin;

import net.minecraftforge.network.ConnectionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ConnectionType.class)
public class Mixin_ConnectionType {

    @Inject(method = "forVersionFlag", at = @At("HEAD"), cancellable = true, remap = false)
    private static void handleUnknownVersionFlag(String vers, CallbackInfoReturnable<ConnectionType> cir) {
        if (vers == null) {
            cir.setReturnValue(ConnectionType.VANILLA);
        }
    }
}
