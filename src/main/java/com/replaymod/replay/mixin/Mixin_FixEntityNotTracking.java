package com.replaymod.replay.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientPacketListener.class)
public class Mixin_FixEntityNotTracking {
    @ModifyVariable(method = { "handleTeleportEntity", "handleMoveEntity", "handleSetEntityPassengersPacket" }, at = @At("RETURN"), ordinal = 0)
    private Entity updatePositionIfNotTracked$0(Entity entity) {
        if (entity != null) {
            entity.getSelfAndPassengers().forEach(this::updatePositionIfNotTracked);
        }
        return entity;
    }

    private void updatePositionIfNotTracked(Entity entity) {
        if (entity != null && entity.level() instanceof ClientWorldAccessor world) {
            if (!world.getTickingEntities().contains(entity)) {
                // This only applies to entities that are server-controlled.
                // You wouldn't think this check is necessary but for some stupid reason Spigot sends entities before
                // chunks, so we must not update the entity if it is being ridden by the client player because those
                // will fall into the void and die (client-side only ofc).
                if (entity.isLocalInstanceAuthoritative()) {
                    return;
                }

                // Skip interpolation of position updates coming from server
                // (See: newX in EntityLivingBase or otherPlayerMPX in EntityOtherPlayerMP)
                int ticks = 0;
                Vec3 prevPos;
                do {
                    prevPos = entity.position();
                    if (entity.isPassenger()) {
                        entity.rideTick();
                    } else {
                        entity.tick();
                    }
                } while (prevPos.distanceToSqr(entity.position()) > 0.0001 && ticks++ < 100);
            }
        }
    }
}
