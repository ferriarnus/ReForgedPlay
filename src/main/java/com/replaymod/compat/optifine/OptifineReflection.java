package com.replaymod.compat.optifine;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.client.Options;

public class OptifineReflection {

    // GameSettings.ofFastRender
    public static Field gameSettings_ofFastRender;

    static {
        try {
            // this throws an ignored ClassNotFoundException if Optifine isn't installed
            Class.forName("Config");

            gameSettings_ofFastRender = Options.class.getDeclaredField("ofFastRender");
            gameSettings_ofFastRender.setAccessible(true);
        } catch (ClassNotFoundException ignore) {
            // no optifine installed
        } catch (NoSuchFieldException e) {
            // the field wasn't found. Has it been renamed?
            e.printStackTrace();
        }
    }

    public static void reloadLang() {
        try {
            Class<?> langClass;
            try {
                langClass = Class.forName("Lang"); // Pre Optifine 1.12.2 E1
            } catch (ClassNotFoundException ignore) {
                langClass = Class.forName("net.optifine.Lang"); // Post Optifine 1.12.2 E1
            }
            langClass.getDeclaredMethod("resourcesReloaded").invoke(null);
        } catch (ClassNotFoundException ignore) {
            // no optifine installed
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
