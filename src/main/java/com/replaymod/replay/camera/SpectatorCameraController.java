package com.replaymod.replay.camera;

import com.replaymod.replay.ReplayModReplay;

//#if MC>=11400
//#else
//$$ import org.lwjgl.input.Mouse;
//#endif

import java.util.Arrays;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import static com.replaymod.core.versions.MCVer.*;

public class SpectatorCameraController implements CameraController {
    private final CameraEntity camera;

    public SpectatorCameraController(CameraEntity camera) {
        this.camera = camera;
    }

    @Override
    public void update(float partialTicksPassed) {
        Minecraft mc = getMinecraft();
        if (mc.options.keyShift.consumeClick()) {
            ReplayModReplay.instance.getReplayHandler().spectateCamera();
        }

        // Soak up all remaining key presses
        for (KeyMapping binding : Arrays.asList(mc.options.keyAttack, mc.options.keyUse,
                mc.options.keyJump, mc.options.keyShift, mc.options.keyUp,
                mc.options.keyDown, mc.options.keyLeft, mc.options.keyRight)) {
            //noinspection StatementWithEmptyBody
            while (binding.consumeClick());
        }

        // Prevent mouse movement
        //#if MC>=11400
        // No longer needed
        //#else
        //$$ Mouse.updateCursor();
        //#endif

        // Always make sure the camera is in the exact same spot as the spectated entity
        // This is necessary as some rendering code for the hand doesn't respect the view entity
        // and always uses mc.thePlayer
        Entity view = mc.getCameraEntity();
        if (view != null && view != camera) {
            camera.setCameraPosRot(mc.getCameraEntity());
        }
    }

    @Override
    public void increaseSpeed() {

    }

    @Override
    public void decreaseSpeed() {

    }
}
