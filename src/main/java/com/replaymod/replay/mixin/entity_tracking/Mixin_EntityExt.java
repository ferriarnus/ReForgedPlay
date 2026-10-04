package com.replaymod.replay.mixin.entity_tracking;

import com.replaymod.replay.ext.EntityExt;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class Mixin_EntityExt implements EntityExt {

    @Shadow
    public float yRot;

    @Shadow
    public float xRot;

    @Unique
    private float trackedYaw = Float.NaN;

    @Unique
    private float trackedPitch = Float.NaN;

    @Override
    public float replaymod$getTrackedYaw() {
        return !Float.isNaN(this.trackedYaw) ? this.trackedYaw : this.yRot;
    }

    @Override
    public float replaymod$getTrackedPitch() {
        return !Float.isNaN(this.trackedPitch) ? this.trackedPitch : this.xRot;
    }

    @Override
    public void replaymod$setTrackedYaw(float value) {
        this.trackedYaw = value;
    }

    @Override
    public void replaymod$setTrackedPitch(float value) {
        this.trackedPitch = value;
    }
}
