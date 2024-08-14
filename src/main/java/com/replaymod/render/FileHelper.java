package com.replaymod.render;

import net.minecraftforge.fml.loading.LoadingModList;

import java.nio.file.Path;

public interface FileHelper {

    static Path getpath(String modid) {
        return LoadingModList.get().getModFileById(modid).getFile().getSecureJar().getRootPath();
    }
}
