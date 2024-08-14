//#if MC>=11600
package com.replaymod.render.mixin;

import com.replaymod.core.ReplayMod;
import com.replaymod.render.FileHelper;
import com.replaymod.render.capturer.IrisODSFrameCapturer;
import net.irisshaders.iris.Iris;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.LoadingModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;

@Pseudo
@Mixin(value = Iris.class, remap = false)
public class Mixin_LoadIrisOdsShaderPack {
    @Redirect(method = "loadExternalShaderpack", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/Iris;getShaderpacksDirectory()Ljava/nio/file/Path;"))
    private static Path loadReplayModOdsPack(String name) {
        if (IrisODSFrameCapturer.INSTANCE != null && IrisODSFrameCapturer.SHADER_PACK_NAME.equals(name)) {
            return FileHelper.getpath(ReplayMod.MOD_ID);
        } else {
            return Iris.getShaderpacksDirectory();
        }
    }
}
//#endif
