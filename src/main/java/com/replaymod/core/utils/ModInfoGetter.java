package com.replaymod.core.utils;
import com.replaymod.replaystudio.data.ModInfo;
import net.minecraft.util.Identifier;

import net.minecraft.registry.Registry;
import net.minecraftforge.fml.loading.LoadingModList;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

class ModInfoGetter {
    static Collection<ModInfo> getInstalledNetworkMods() {
        Map<String, ModInfo> modInfoMap = LoadingModList.get().getMods().stream()
                .map(m -> new ModInfo(m.getModId(), m.getDisplayName(), m.getVersion().toString()))
                .collect(Collectors.toMap(ModInfo::getId, Function.identity()));
        return net.minecraft.registry.Registries.REGISTRIES.stream()
                .map(Registry::getIds).flatMap(Set::stream)
                .map(Identifier::getNamespace).filter(s -> !s.equals("minecraft")).distinct()
                .map(modInfoMap::get).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
