package com.replaymod.replay.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
//#if MC>=10904
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Queue;

@Mixin(ParticleEngine.class)
public abstract class MixinParticleManager {
    @Final @Shadow
    private Queue<Particle> particlesToAdd;

    /**
     * This method additionally clears the queue of particles to be added when the world is changed.
     * Otherwise particles from the previous world might show up in this one if they were spawned after
     * the last tick in the previous world.
     *
     * @param world The new world
     * @param ci Callback info
     */
    @Inject(method = "setLevel", at = @At("HEAD"))
    public void replayModReplay_clearParticleQueue(
            //#if MC>=11600
            ClientLevel world,
            //#else
            //$$ World world,
            //#endif
            CallbackInfo ci) {
        this.particlesToAdd.clear();
    }
}
//#endif
