package com.replaymod.recording.mixin;

import net.minecraftforge.network.HandshakeMessages;
import net.minecraftforge.registries.RegistryManager;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;

import static net.minecraftforge.registries.RegistryManager.ACTIVE;

@Mixin(RegistryManager.class)
public class Mixin_ForgeSync {

    @Inject(method = "generateRegistryPackets", at = @At("HEAD"), remap = false, cancellable = true)
    private static void doSync(boolean isLocal, CallbackInfoReturnable<List<Pair<String, HandshakeMessages.S2CRegistry>>> cir) {
        cir.setReturnValue(ACTIVE.takeSnapshot(false).entrySet().stream().
                map(e->Pair.of("Registry " + e.getKey(), new HandshakeMessages.S2CRegistry(e.getKey(), e.getValue()))).
                collect(Collectors.toList()));
    }
}
