package com.replaymod.replay.handler;

import com.replaymod.core.gui.GuiReplayButton;
import com.replaymod.replay.Setting;
import com.replaymod.replay.ScreenButtonExtension;
import de.johni0702.minecraft.gui.container.VanillaGuiScreen;
import de.johni0702.minecraft.gui.element.GuiTooltip;
import de.johni0702.minecraft.gui.layout.CustomLayout;
import de.johni0702.minecraft.gui.utils.EventRegistrations;
import de.johni0702.minecraft.gui.utils.lwjgl.Point;
import de.johni0702.minecraft.gui.versions.callbacks.InitScreenCallback;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.gui.screen.GuiReplayViewer;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

//#if MC>=12000
import net.minecraft.client.gui.DrawContext;
//#endif

//#if MC>=11904
//#else
//$$ import net.minecraft.client.MinecraftClient;
//#endif

//#if MC>=11903
import net.minecraft.client.gui.tooltip.Tooltip;
//#endif

//#if MC>=11604
import de.johni0702.minecraft.gui.MinecraftGuiRenderer;
//#endif

//#if MC>=11600
import net.minecraft.text.Text;
//#else
//$$ import net.minecraft.client.resource.language.I18n;
//#endif

//#if FABRIC<1
//$$ import net.minecraftforge.client.event.GuiScreenEvent;
//$$ import net.minecraftforge.eventbus.api.SubscribeEvent;
//#endif

//#if MC>=11400
import net.minecraft.client.gui.widget.ButtonWidget;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
//#endif

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.replaymod.core.versions.MCVer.*;
import static com.replaymod.replay.ReplayModReplay.LOGGER;

public class GuiHandler extends EventRegistrations {
    private static final int BUTTON_REPLAY_VIEWER = 17890234;
    private static final int BUTTON_EXIT_REPLAY = 17890235;

    private final ReplayModReplay mod;

    public GuiHandler(ReplayModReplay mod) {
        this.mod = mod;
    }

    { on(InitScreenCallback.EVENT, this::injectIntoIngameMenu); }
    private void injectIntoIngameMenu(Screen guiScreen, Collection<ClickableWidget> buttonList) {
        if (!(guiScreen instanceof GameMenuScreen)) {
            return;
        }

        if (mod.getReplayHandler() != null) {
            // Pause replay when menu is opened
            mod.getReplayHandler().getReplaySender().setReplaySpeed(0);

            //#if MC>=11600
            final Text BUTTON_OPTIONS = net.minecraft.text.Text.translatable("menu.options");
            final Text BUTTON_EXIT_SERVER = net.minecraft.text.Text.translatable("menu.disconnect");
            final Text BUTTON_ADVANCEMENTS = net.minecraft.text.Text.translatable("gui.advancements");
            final Text BUTTON_STATS = net.minecraft.text.Text.translatable("gui.stats");
            final Text BUTTON_OPEN_TO_LAN = net.minecraft.text.Text.translatable("menu.shareToLan");
            //#else
            //#if MC>=11400
            //$$ final String BUTTON_OPTIONS = I18n.translate("menu.options");
            //$$ final String BUTTON_EXIT_SERVER = I18n.translate("menu.disconnect");
            //$$ final String BUTTON_ADVANCEMENTS = I18n.translate("gui.advancements");
            //$$ final String BUTTON_STATS = I18n.translate("gui.stats");
            //$$ final String BUTTON_OPEN_TO_LAN = I18n.translate("menu.shareToLan");
            //#else
            //#if MC>=11400
            //$$ final int BUTTON_OPTIONS = 0;
            //#endif
            //$$ final int BUTTON_EXIT_SERVER = 1;
            //$$ final int BUTTON_ADVANCEMENTS = 5;
            //$$ final int BUTTON_STATS = 6;
            //$$ final int BUTTON_OPEN_TO_LAN = 7;
            //#endif
            //#endif


            //#if MC<11400
            //$$ GuiButton openToLan = null;
            //#endif
            //#if MC>=11400
            ClickableWidget achievements = null, stats = null;
            for(ClickableWidget b : new ArrayList<>(buttonList)) {
            //#else
            //$$ GuiButton achievements = null, stats = null;
            //$$ for(GuiButton b : new ArrayList<>(buttonList)) {
            //#endif
                boolean remove = false;
                //#if MC>=11400
                //#if MC>=11600
                Text id = b.getMessage();
                //#else
                //$$ String id = b.getMessage();
                //#endif
                if (id == null) {
                    // likely a button of some third-part mod
                    // e.g. https://github.com/Pokechu22/WorldDownloader/blob/b1b279f948beec2d7dac7524eea8f584a866d8eb/share_14/src/main/java/wdl/WDLHooks.java#L491
                    continue;
                }
                //#else
                //$$ Integer id = b.id;
                //#endif
                if (id.equals(BUTTON_EXIT_SERVER)) {
                    // Replace "Exit Server" button with "Exit Replay" button
                    remove = true;
                    addButton(guiScreen, new InjectedButton(
                            guiScreen,
                            BUTTON_EXIT_REPLAY,
                            b.getX(),
                            b.getY(),
                            b.getWidth(),
                            b.getHeight(),
                            "replaymod.gui.exit",
                            null,
                            this::onButton
                    ));
                } else if (id.equals(BUTTON_ADVANCEMENTS)) {
                    // Remove "Advancements", "Stats" and "Open to LAN" buttons
                    remove = true;
                    achievements = b;
                } else if (id.equals(BUTTON_STATS)) {
                    remove = true;
                    stats = b;
                } else if (id.equals(BUTTON_OPEN_TO_LAN)) {
                    remove = true;
                    //#if MC<11400
                    //$$ openToLan = b;
                    //#endif
                //#if MC>=11400 && MC<11901
                //$$ } else if (id.equals(BUTTON_OPTIONS)) {
                    //#if MC>=11400
                    //$$ b.setWidth(204);
                    //#else
                    //$$ b.width = 200
                    //#endif
                //#endif
                }
                if (remove) {
                    // Moving the button far off-screen is easier to do cross-version than actually removing it
                    b.setX(-1000);
                    b.setY(-1000);
                }
            }
            if (achievements != null && stats != null) {
                moveAllButtonsInRect(buttonList,
                        achievements.getX(), stats.getX() + stats.getWidth(),
                        achievements.getY(), Integer.MAX_VALUE,
                        -24);
            }
            // In 1.13+ Forge, the Options button shares one row with the Open to LAN button
            //#if MC<11400
            //$$ if (openToLan != null) {
            //$$     moveAllButtonsInRect(buttonList,
            //$$             openToLan.x, openToLan.x + openToLan.width,
            //$$             openToLan.y, Integer.MAX_VALUE,
            //$$             -24);
            //$$ }
            //#endif
        }
    }

    /**
     * Moves all buttons that in any way intersect a rectangle by a given amount on the y axis.
     * @param buttons List of buttons
     * @param yStart Top y limit of the rectangle
     * @param yEnd Bottom y limit of the rectangle
     * @param xStart Left x limit of the rectangle
     * @param xEnd Right x limit of the rectangle
     * @param moveBy Signed distance to move the buttons
     */
    private void moveAllButtonsInRect(
            //#if MC>=11400
            Collection<ClickableWidget> buttons,
            //#else
            //$$ Collection<GuiButton> buttons,
            //#endif
            int xStart,
            int xEnd,
            int yStart,
            int yEnd,
            int moveBy
    ) {
        buttons.stream()
                .filter(button -> button.getX() <= xEnd && button.getX() + button.getWidth() >= xStart)
                .filter(button -> button.getY() <= yEnd && button.getY() + button.getHeight() >= yStart)
                // FIXME remap bug: needs the {} to recognize the setter (it also doesn't understand +=)
                .forEach(button -> { button.setY(button.getY() + moveBy); });
    }

    { on(InitScreenCallback.EVENT, (screen, buttons) -> ensureReplayStopped(screen)); }
    private void ensureReplayStopped(Screen guiScreen) {
        if (!(guiScreen instanceof TitleScreen || guiScreen instanceof MultiplayerScreen)) {
            return;
        }

        if (mod.getReplayHandler() != null) {
            // Something went terribly wrong and we ended up in the main menu with the replay still active.
            // To prevent players from joining live servers and using the CameraEntity, try to stop the replay now.
            try {
                mod.getReplayHandler().endReplay();
            } catch (IOException e) {
                LOGGER.error("Trying to stop broken replay: ", e);
            } finally {
                if (mod.getReplayHandler() != null) {
                    mod.forcefullyStopReplay();
                }
            }
        }
    }

    { on(InitScreenCallback.EVENT, this::injectIntoMainMenu); }
    private void injectIntoMainMenu(Screen guiScreen, Collection<ClickableWidget> buttonList) {
        if (!(guiScreen instanceof TitleScreen)) {
            return;
        }

        //#if MC>=11604
        if (mod.getCore().getSettingsRegistry().get(Setting.LEGACY_MAIN_MENU_BUTTON)) {
            legacyInjectIntoMainMenu(guiScreen, buttonList);
        } else {
            properInjectIntoMainMenu(guiScreen);
        }
        //#else
        //$$ legacyInjectIntoMainMenu(guiScreen, buttonList);
        //#endif
    }

    //#if MC>=11604
    private void properInjectIntoMainMenu(Screen screen) {
        List<ClickableWidget> buttonList = ((ScreenButtonExtension) screen).replay_getButtons();
        MainMenuButtonPosition buttonPosition = MainMenuButtonPosition.valueOf(mod.getCore().getSettingsRegistry().get(Setting.MAIN_MENU_BUTTON));

        // Workaround for FancyMenu v2 initializing the screen twice, likely fixed in v3
        if (isFancyMenu2Installed()) {
            for (ClickableWidget button : buttonList) {
                if (button instanceof InjectedButton) {
                    return;
                }
            }
        }

        Point pos;
        if (buttonPosition == MainMenuButtonPosition.BIG) {
            int x = screen.width / 2 - 100;
            // We want to position our button below the realms button
            Optional<ClickableWidget> targetButton = findButton(buttonList, "menu.online", 14)
                    .map(Optional::of)
                    // or, if someone removed the realms button, we'll alternatively take the multiplayer one
                    .orElseGet(() -> findButton(buttonList, "menu.multiplayer", 2));

            int y = targetButton
                    // if we found some button, put our button at its position (we'll move it out of the way shortly)
                    .map(it -> it.getY())
                    // and if we can't even find that one, then just guess
                    .orElse(screen.height / 4 + 10 + 4 * 24);

            // Move all buttons above or at our one upwards
            moveAllButtonsInRect(buttonList,
                    x, x + 200,
                    Integer.MIN_VALUE, y,
                    -24);

            pos = new Point(x, y);
        } else {
            pos = determineButtonPos(buttonPosition, screen, buttonList);
        }

        ClickableWidget replayViewerButton;
        if (buttonPosition == MainMenuButtonPosition.BIG) {
            replayViewerButton = new InjectedButton(
                    screen, BUTTON_REPLAY_VIEWER,
                    pos.getX(), pos.getY(),
                    200, 20,
                    "replaymod.gui.replayviewer",
                    null,
                    this::onButton
            );
        } else {
            replayViewerButton = new InjectedButton(
                    screen, BUTTON_REPLAY_VIEWER,
                    pos.getX(), pos.getY(),
                    20, 20,
                    "",
                    "replaymod.gui.replayviewer",
                    this::onButton
            ) {
                @Override
                //#if MC>=12000
                public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                    super.renderWidget(context, mouseX, mouseY, delta);
                //#elseif MC>=11904
                //$$ public void renderButton(MatrixStack context, int mouseX, int mouseY, float delta) {
                //$$     super.renderButton(context, mouseX, mouseY, delta);
                //#else
                //$$ protected void renderBackground(MatrixStack context, MinecraftClient client, int mouseX, int mouseY) {
                //$$     super.renderBackground(context, client, mouseX, mouseY);
                //#endif

                    MinecraftGuiRenderer renderer = new MinecraftGuiRenderer(context);
                    renderer.bindTexture(GuiReplayButton.ICON);
                    renderer.drawTexturedRect(
                            this.getX() + 3, this.getY() + 3,
                            0, 0,
                            this.width - 6, this.height - 6,
                            1, 1,
                            1, 1
                    );
                }
            };
        }

        int index = determineButtonIndex(buttonList, replayViewerButton);
        if (index != -1) {
            buttonList.add(index, replayViewerButton);
        } else {
            buttonList.add(replayViewerButton);
        }
    }

    private boolean isFancyMenu2Installed() {
        ModFileInfo mod = FMLLoader.getLoadingModList().getModFileById("fancymenu");
        if (mod == null) {
            return false;
        }
        return mod.getMods().get(0).getVersion().toString().startsWith("2.");
    }
    //#endif

    private void legacyInjectIntoMainMenu(Screen guiScreen, Collection<ClickableWidget> buttonList) {
        boolean isCustomMainMenuMod = guiScreen.getClass().getName().endsWith("custommainmenu.gui.GuiFakeMain");

        MainMenuButtonPosition buttonPosition = MainMenuButtonPosition.valueOf(mod.getCore().getSettingsRegistry().get(Setting.MAIN_MENU_BUTTON));
        if (buttonPosition != MainMenuButtonPosition.BIG && !isCustomMainMenuMod) {
            VanillaGuiScreen vanillaGui = VanillaGuiScreen.wrap(guiScreen);

            GuiReplayButton replayButton = new GuiReplayButton();
            replayButton
                    .onClick(() -> new GuiReplayViewer(mod).display())
                    .setTooltip(new GuiTooltip().setI18nText("replaymod.gui.replayviewer"));

            vanillaGui.setLayout(new CustomLayout<de.johni0702.minecraft.gui.container.GuiScreen>(vanillaGui.getLayout()) {
                private Point pos;

                @Override
                protected void layout(de.johni0702.minecraft.gui.container.GuiScreen container, int width, int height) {
                    if (pos == null) {
                        // Delaying computation so we can take into account buttons
                        // added after our callback.
                        pos = determineButtonPos(buttonPosition, guiScreen, buttonList);
                    }
                    size(replayButton, 20, 20);
                    pos(replayButton, pos.getX(), pos.getY());
                }
            }).addElements(null, replayButton);
            return;
        }

        int x = guiScreen.width / 2 - 100;
        // We want to position our button below the realms button
        int y = findButton(buttonList, "menu.online", 14)
                .map(Optional::of)
                // or, if someone removed the realms button, we'll alternatively take the multiplayer one
                .orElse(findButton(buttonList, "menu.multiplayer", 2))
                // if we found some button, put our button at its position (we'll move it out of the way shortly)
                .map(it -> it.getY())
                // and if we can't even find that one, then just guess
                .orElse(guiScreen.height / 4 + 10 + 4 * 24);

        // Move all buttons above or at our one upwards
        moveAllButtonsInRect(buttonList,
                x, x + 200,
                Integer.MIN_VALUE, y,
                -24);

        // Add our button
        InjectedButton button = new InjectedButton(
                guiScreen,
                BUTTON_REPLAY_VIEWER,
                x,
                y,
                200,
                20,
                "replaymod.gui.replayviewer",
                null,
                this::onButton
        );
        //#if FABRIC<=0
        //$$ if (isCustomMainMenuMod) {
        //$$     // CustomMainMenu uses a different list in the event than in its Fake gui
        //$$     buttonList.add(button);
        //$$     return;
        //$$ }
        //#endif
        addButton(guiScreen, button);
    }

    private Point determineButtonPos(MainMenuButtonPosition buttonPosition, Screen guiScreen, Collection<ClickableWidget> buttonList) {
        Point topRight = new Point(guiScreen.width - 20 - 5, 5);

        if (buttonPosition == MainMenuButtonPosition.TOP_LEFT) {
            return new Point(5, 5);
        } else if (buttonPosition == MainMenuButtonPosition.TOP_RIGHT) {
            return topRight;
        } else if (buttonPosition == MainMenuButtonPosition.DEFAULT) {
            return Stream.of(
                    findButton(buttonList, "menu.singleplayer", 1),
                    findButton(buttonList, "menu.multiplayer", 2),
                    findButton(buttonList, "menu.online", 14),
                    findButton(buttonList, "modmenu.title", 6)
            )
                    // skip buttons which do not exist
                    .flatMap(it -> it.map(Stream::of).orElseGet(Stream::empty))
                    // skip buttons which already have something next to them
                    .filter(it -> buttonList.stream().noneMatch(button ->
                            button.getX() <= it.getX() + it.getWidth() + 4 + 20
                                    && button.getY() <= it.getY() + it.getHeight()
                                    && button.getX() + button.getWidth() >= it.getX() + it.getWidth() + 4
                                    && button.getY() + button.getHeight() >= it.getY()
                    ))
                    // then take the bottom-most and if there's two, the right-most
                    .max(Comparator.<ClickableWidget>comparingInt(it -> it.getY()).thenComparingInt(it -> it.getX()))
                    // and place ourselves next to it
                    .map(it -> new Point(it.getX() + it.getWidth() + 4, it.getY()))
                    // if all fails, just go with TOP_RIGHT
                    .orElse(topRight);
        } else {
            return Optional.of(buttonList).flatMap(buttons -> {
                switch (buttonPosition) {
                    case LEFT_OF_SINGLEPLAYER:
                    case RIGHT_OF_SINGLEPLAYER:
                        return findButton(buttons, "menu.singleplayer", 1);
                    case LEFT_OF_MULTIPLAYER:
                    case RIGHT_OF_MULTIPLAYER:
                        return findButton(buttons, "menu.multiplayer", 2);
                    case LEFT_OF_REALMS:
                    case RIGHT_OF_REALMS:
                        return findButton(buttons, "menu.online", 14);
                    case LEFT_OF_MODS:
                    case RIGHT_OF_MODS:
                        return findButton(buttons, "modmenu.title", 6);
                }
                throw new RuntimeException();
            }).map(button -> {
                switch (buttonPosition) {
                    case LEFT_OF_SINGLEPLAYER:
                    case LEFT_OF_MULTIPLAYER:
                    case LEFT_OF_REALMS:
                    case LEFT_OF_MODS:
                        return new Point(button.getX() - 4 - 20, button.getY());
                    case RIGHT_OF_MODS:
                    case RIGHT_OF_SINGLEPLAYER:
                    case RIGHT_OF_MULTIPLAYER:
                    case RIGHT_OF_REALMS:
                        return new Point(button.getX() + button.getWidth() + 4, button.getY());
                }
                throw new RuntimeException();
            }).orElse(topRight);
        }
    }

    private int determineButtonIndex(Collection<ClickableWidget> buttons, ClickableWidget button) {
        ClickableWidget best = null;
        int bestIndex = -1;

        int index = 0;
        for (ClickableWidget other : buttons) {
            if (other.getY() > button.getY() || other.getY() == button.getY() && other.getX() > button.getX()) {
                index++;
                continue;
            }

            if (best == null || other.getY() > best.getY() || other.getY() == best.getY() && other.getX() > best.getX()) {
                best = other;
                bestIndex = index + 1;
            }

            index++;
        }
        return bestIndex;
    }

    //#if MC>=11400
    private void onButton(InjectedButton button) {
        Screen guiScreen = button.guiScreen;
    //#else
    //$$ @SubscribeEvent
    //$$ public void onButton(GuiScreenEvent.ActionPerformedEvent.Pre event) {
    //$$     GuiScreen guiScreen = event.getGui();
    //$$     GuiButton button = event.getButton();
    //#endif
        if(!button.active) return;

        if (guiScreen instanceof TitleScreen) {
            if (button.id == BUTTON_REPLAY_VIEWER) {
                new GuiReplayViewer(mod).display();
            }
        }

        if (guiScreen instanceof GameMenuScreen && mod.getReplayHandler() != null) {
            if (button.id == BUTTON_EXIT_REPLAY) {
                button.active = false;
                try {
                    mod.getReplayHandler().endReplay();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class InjectedButton extends
            //#if MC>=11400
            ButtonWidget
            //#else
            //$$ GuiButton
            //#endif
    {
        public final Screen guiScreen;
        public final int id;
        private Consumer<InjectedButton> onClick;
        public InjectedButton(Screen guiScreen, int buttonId, int x, int y, int width, int height, String buttonText,
                              String tooltip,
                              //#if MC>=11400
                              Consumer<InjectedButton> onClick
                              //#else
                              //$$ Consumer<GuiScreenEvent.ActionPerformedEvent.Pre> onClick
                              //#endif
        ) {
            super(
                    //#if MC<11400
                    //$$ buttonId,
                    //#endif
                    x,
                    y,
                    width,
                    height,
                    //#if MC>=11600
                    net.minecraft.text.Text.translatable(buttonText)
                    //#else
                    //$$ I18n.translate(buttonText)
                    //#endif
                    //#if MC>=11400
                    , self -> onClick.accept((InjectedButton) self)
                    //#endif
                    //#if MC>=11600 && MC<11903
                    //$$ , tooltip != null
                    //$$         ? (button, matrices, mouseX, mouseY) -> guiScreen.renderTooltip(matrices, net.minecraft.text.Text.translatable(tooltip), mouseX, mouseY)
                    //$$         : EMPTY
                    //#endif
                    //#if MC>=11903
                    , DEFAULT_NARRATION_SUPPLIER
                    //#endif
            );
            this.guiScreen = guiScreen;
            this.id = buttonId;
            //#if MC>=11400
            this.onClick = onClick;
            //#else
            //$$ this.onClick = null;
            //#endif

            //#if MC>=11903
            if (tooltip != null) {
                setTooltip(Tooltip.of(Text.translatable(tooltip)));
            }
            //#endif
        }

        //#if MC>=11400 && MC<11400
        //$$ @Override
        //$$ public void onClick(double mouseX, double mouseY) {
        //$$     onClick.accept(this);
        //$$ }
        //#endif
    }

    public enum MainMenuButtonPosition {
        // The old big button below Realms/Mods which pushes other buttons around.
        BIG,
        // Right of the bottom-most button in the main block of buttons (so not the quit button).
        // That will generally be either RIGHT_OF_REALMS or RIGHT_OF_MODS depending on version and installed mods.
        DEFAULT,
        TOP_LEFT,
        TOP_RIGHT,
        LEFT_OF_SINGLEPLAYER,
        RIGHT_OF_SINGLEPLAYER,
        LEFT_OF_MULTIPLAYER,
        RIGHT_OF_MULTIPLAYER,
        LEFT_OF_REALMS,
        RIGHT_OF_REALMS,
        LEFT_OF_MODS,
        RIGHT_OF_MODS,
    }
}
