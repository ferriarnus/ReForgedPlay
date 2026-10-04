package com.replaymod.replay.mixin.entity_tracking;

import com.replaymod.replay.ext.EntityExt;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Every Entity on the client has at least two position/rotation states: The local one and the server ("tracked") one.
 * When receiving an update from the server, the server one needs to be updated and the local one will then usually be
 * interpolated to the server one within a few ticks.
 *
 * Minecraft however incorrectly implements the server rotation update for position-only update packets by
 * interpolating to the local rotation, which might not yet match the server rotation, rather than to the previously
 * received server rotation.
 * Similarly, with 1.15 and later, it incorrectly implements the server position update for rotation-only update packets
 * by interpolating to the local position rather than the server one.
 *
 * Each of these will cause the client position to be in an incorrect state until the next update packed for the
 * respective rotation/position of that entity.
 *
 * This mixin fixes those two issues by redirecting to the server rotation/position respectively.
 * Minecraft does not currently even track the server rotation, so we need to do that as well.
 */
@Mixin(ClientPacketListener.class)
public class Mixin_FixPartialUpdates {

    //
    // Use correct rotation for position-only updates
    //

    //#if MC>=11700
    @Redirect(method = "handleMoveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getYRot()F"))
    //#else
    //$$ @Redirect(method = "onEntityUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;yaw:F", opcode = Opcodes.GETFIELD))
    //#endif
    private float getTrackedYaw(Entity instance) {
        return ((EntityExt) instance).replaymod$getTrackedYaw();
    }

    //#if MC>=11700
    @Redirect(method = "handleMoveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getXRot()F"))
    //#else
    //$$ @Redirect(method = "onEntityUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;pitch:F", opcode = Opcodes.GETFIELD))
    //#endif
    private float getTrackedPitch(Entity instance) {
        return ((EntityExt) instance).replaymod$getTrackedPitch();
    }

    //#if MC>=11500
    //
    // Use correct position for rotation-only updates
    //
    // Except for the special case of the entity riding another entity because of a second vanilla bug where it won't
    // send tracked position updates to the client while the entity is riding...
    // Nothing we can do in that case, fixing that would require modifying the server.
    //

    @Redirect(method = "handleMoveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getX()D"))
    private double getTrackedX(Entity instance) {
        return instance.isPassenger() ? instance.getX() : instance.getPositionCodec().decode(0,0,0).x();
    }

    @Redirect(method = "handleMoveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getY()D"))
    private double getTrackedY(Entity instance) {
        return instance.isPassenger() ? instance.getY() : instance.getPositionCodec().decode(0,0,0).y();
    }

    @Redirect(method = "handleMoveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getZ()D"))
    private double getTrackedZ(Entity instance) {
        return instance.isPassenger() ? instance.getZ() : instance.getPositionCodec().decode(0,0,0).z();
    }
    //#endif

    //
    // Track server rotation
    //

    private static final String ENTITY_UPDATE = "Lnet/minecraft/entity/Entity;updateTrackedPositionAndAngles(DDDFFIZ)V";

    @Unique
    private Entity entity;

    @ModifyVariable(method = { "handleMoveEntity", "handleTeleportEntity" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;updateTrackedPositionAndAngles(DDDFFIZ)V"), ordinal = 0)
    private Entity captureEntity(Entity entity) {
        return this.entity = entity;
    }

    @Inject(method = { "handleMoveEntity", "handleTeleportEntity" }, at = @At("RETURN"))
    private void resetEntityField(CallbackInfo ci) {
        this.entity = null;
    }

    @ModifyArg(method = { "handleMoveEntity", "handleTeleportEntity" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;updateTrackedPositionAndAngles(DDDFFIZ)V"), index = 3)
    private float captureTrackedYaw(float value) {
        ((EntityExt) this.entity).replaymod$setTrackedYaw(value);
        return value;
    }

    @ModifyArg(method = { "handleMoveEntity", "handleTeleportEntity" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;updateTrackedPositionAndAngles(DDDFFIZ)V"), index = 4)
    private float captureTrackedPitch(float value) {
        ((EntityExt) this.entity).replaymod$setTrackedPitch(value);
        return value;
    }
}