/*
 * This file is part of jGui API, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 johni0702 <https://github.com/johni0702>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.johni0702.minecraft.gui.utils;

import com.mojang.blaze3d.platform.Window;
import de.johni0702.minecraft.gui.utils.lwjgl.Point;
import de.johni0702.minecraft.gui.versions.MCVer;
import net.minecraft.client.Minecraft;

public class MouseUtils {
    private static final Minecraft mc = MCVer.getMinecraft();

    public static Point getMousePos() {
        //#if MC>=11400
        int mouseX = (int) mc.mouseHandler.xpos();
        int mouseY = (int) mc.mouseHandler.ypos();
        Window mainWindow = MCVer.newScaledResolution(mc);
        mouseX = (int) Math.round((double) mouseX * mainWindow.getGuiScaledWidth() / mainWindow.getScreenWidth());
        mouseY = (int) Math.round((double) mouseY * mainWindow.getGuiScaledHeight() / mainWindow.getScreenHeight());
        //#else
        //$$ Point scaled = getScaledDimensions();
        //$$ int width = scaled.getX();
        //$$ int height = scaled.getY();
        //$$
        //$$ int mouseX = (Mouse.getX() * width / mc.displayWidth);
        //$$ int mouseY = (height - Mouse.getY() * height / mc.displayHeight);
        //#endif

        return new Point(mouseX, mouseY);
    }

    public static Point getScaledDimensions() {
        //#if MC>=11400
        Window
        //#else
        //$$ ScaledResolution
        //#endif
                res = MCVer.newScaledResolution(mc);
        return new Point(res.getGuiScaledWidth(), res.getGuiScaledHeight());
    }
}
