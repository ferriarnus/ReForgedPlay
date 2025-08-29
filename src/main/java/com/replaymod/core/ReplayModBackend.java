package com.replaymod.core;

import net.minecraft.SharedConstants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.LoadingModList;

import static com.replaymod.core.ReplayMod.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
@Mod(MOD_ID)
public class ReplayModBackend {
    private final ReplayMod mod = new ReplayMod(this);

    @SubscribeEvent
    public static void construct(FMLLoadCompleteEvent event) {
        ReplayMod.instance.initModules();
    }

    public String getVersion() {
        return LoadingModList.get().getModFileById(MOD_ID)
                .versionString();
    }

    public String getMinecraftVersion() {
        //#if MC>=12106
        return SharedConstants.getGameVersion().name();
        //#else
        //$$ return SharedConstants.getGameVersion().getName();
        //#endif
    }

    public boolean isModLoaded(String id) {
        return LoadingModList.get().getModFileById(id.toLowerCase()) != null;
    }
}
