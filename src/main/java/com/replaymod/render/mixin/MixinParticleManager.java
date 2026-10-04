package com.replaymod.render.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.replaymod.core.versions.MCVer;
import com.replaymod.render.hooks.EntityRendererHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SingleQuadParticle.class)
public abstract class MixinParticleManager {
    @Shadow public abstract void extract(QuadParticleRenderState par1, Camera par2, float par3);

    @Inject(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/SingleQuadParticle$FacingCameraMode;setRotation(Lorg/joml/Quaternionf;Lnet/minecraft/client/Camera;F)V"))
    private void faceCameraAtParticle(
            CallbackInfo ci,
            @Local(argsOnly = true) float partialTicks,
            @Local(argsOnly = true) Camera camera,
            @Share("orgRotation") LocalRef<Quaternionf> orgRotationRef
    ) {
        EntityRendererHandler handler = ((EntityRendererHandler.IEntityRenderer) MCVer.getMinecraft().gameRenderer).replayModRender_getHandler();
        if (handler == null || !handler.omnidirectional) {
            return;
        }

        Quaternionf rotation = camera.rotation();
        orgRotationRef.set(new Quaternionf(rotation));

        Vec3 from = new Vec3(0, 0, -1);
        Vec3 to = MCVer.getPosition((SingleQuadParticle)(Object) this, partialTicks)
                .subtract(camera.position())
                .normalize();
        Vec3 axis = from.cross(to);
        rotation.set((float) axis.x, (float) axis.y, (float) axis.z, (float) (1 + from.dot(to)));
        rotation.normalize();
    }

    @Inject(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/SingleQuadParticle$FacingCameraMode;setRotation(Lorg/joml/Quaternionf;Lnet/minecraft/client/Camera;F)V", shift = At.Shift.AFTER))
    private void faceCameraAtParticleCleanup(
            CallbackInfo ci,
            @Local(argsOnly = true) Camera camera,
            @Share("orgRotation") LocalRef<Quaternionf> orgRotationRef
    ) {
        Quaternionf orgRotation = orgRotationRef.get();
        if (orgRotation == null) {
            return;
        }

        camera.rotation().set(orgRotation.x, orgRotation.y, orgRotation.z, orgRotation.w);
    }
}