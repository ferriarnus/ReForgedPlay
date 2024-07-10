package com.replaymod.core;

import net.minecraft.SharedConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.loading.LoadingModList;

import static com.replaymod.core.ReplayMod.MOD_ID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MOD_ID, value = Dist.CLIENT)
@Mod(MOD_ID)
public class ReplayModBackend {
    private final ReplayMod mod = new ReplayMod(this);

    @SubscribeEvent
    public static void construct(FMLConstructModEvent event) {
        ReplayMod.instance.initModules();
    }

    public String getVersion() {
        return LoadingModList.get().getModFileById(MOD_ID)
                .versionString();
    }

    public String getMinecraftVersion() {
        return SharedConstants.getGameVersion().getName();
    }

    public boolean isModLoaded(String id) {
        return LoadingModList.get().getModFileById(id.toLowerCase()) != null;
    }
}
