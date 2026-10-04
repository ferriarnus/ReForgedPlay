package com.replaymod.recording.mixin;

//#if MC>=10904
import com.replaymod.recording.handler.RecordingEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ClientLevel.class)
public abstract class MixinWorldClient extends Level implements RecordingEventHandler.RecordingEventSender {
    @Shadow
    private Minecraft minecraft;

    @SuppressWarnings("ConstantConditions")
    protected MixinWorldClient() {
        //#if MC>=12102
        super(null, null, null, null, false, false, 0, 0);
        //#elseif MC>=11904
        //$$ super(null, null, null, null, null, false, false, 0, 0);
        //#elseif MC>=11900
        //$$ super(null, null, null, null, false, false, 0, 0);
        //#elseif MC>=11602
        //$$ super(null, null, null, null, false, false, 0);
        //#elseif MC>=11600
        //$$ super(null, null, null, null, null, false, false, 0);
        //#else
        //$$ super(null, null, null, null, false);
        //#endif
    }

    private RecordingEventHandler replayModRecording_getRecordingEventHandler() {
        return ((RecordingEventHandler.RecordingEventSender) this.minecraft.levelRenderer).getRecordingEventHandler();
    }

    // Sounds that are emitted by thePlayer no longer take the long way over the server
    // but are instead played directly by the client. The server only sends these sounds to
    // other clients so we have to record them manually.
    // E.g. Block place sounds
    //#if MC>=12105
    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
            at = @At("HEAD"))
    //#elseif MC>=11903
    //$$ @Inject(method = "playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/sound/SoundCategory;FFJ)V",
    //$$         at = @At("HEAD"))
    //#elseif MC>=11900
    //$$ @Inject(method = "playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFJ)V",
    //$$         at = @At("HEAD"))
    //#elseif MC>=11400
    //#if FABRIC>=1
    //$$ @Inject(method = "playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V",
    //$$         at = @At("HEAD"))
    //#else
    //$$ @Inject(method = "playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V",
    //$$         at = @At("HEAD"))
    //#endif
    //#else
    //$$ @Inject(method = "playSound(Lnet/minecraft/entity/player/EntityPlayer;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V",
    //$$         at = @At("HEAD"))
    //#endif
    public void replayModRecording_recordClientSound(
            //#if MC>=12105
            Entity player,
            //#else
            //$$ PlayerEntity player,
            //#endif
            double x, double y, double z,
            //#if MC>=11903
            Holder<SoundEvent> sound,
            //#else
            //$$ SoundEvent sound,
            //#endif
            SoundSource category,
            float volume, float pitch,
            //#if MC>=11900
            long seed,
            //#endif
            CallbackInfo ci) {
        if (player == this.minecraft.player) {
            RecordingEventHandler handler = replayModRecording_getRecordingEventHandler();
            if (handler != null) {
                // Sent to all other players in ServerWorldEventHandler#playSoundToAllNearExcept
                handler.onPacket(new ClientboundSoundPacket(
                        sound, category, x, y, z, volume, pitch
                        //#if MC>=11900
                        , seed
                        //#endif
                ));
            }
        }
    }

    // Same goes for level events (also called effects). E.g. door open, block break, etc.
    //#if MC>=11400
    //#if MC>=11600
    @Inject(method = "levelEvent", at = @At("HEAD"))
    //#else
    //$$ @Inject(method = "playLevelEvent", at = @At("HEAD"))
    //#endif
    private void playLevelEvent(
            //#if MC>=12105
            Entity player,
            //#else
            //$$ PlayerEntity player,
            //#endif
            int type, BlockPos pos, int data, CallbackInfo ci
    ) {    //#else
    //$$ // These are handled in the World class, so we override the method in WorldClient and add our special handling.
    //$$ @Override
    //$$ public void playEvent (EntityPlayer player, int type, BlockPos pos, int data) {
    //#endif
        if (player == this.minecraft.player) {
            // We caused this event, the server won't send it to us
            RecordingEventHandler handler = replayModRecording_getRecordingEventHandler();
            if (handler != null) {
                handler.onClientEffect(type, pos, data);
            }
        }
        //#if MC<11400
        //$$ super.playEvent(player, type, pos, data);
        //#endif
    }
}
//#endif
