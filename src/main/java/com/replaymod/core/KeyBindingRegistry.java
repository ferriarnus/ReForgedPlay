package com.replaymod.core;

import com.github.steveice10.netty.util.DomainNameMappingBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.platform.InputConstants;
import com.replaymod.core.events.PreRenderCallback;
import com.replaymod.core.mixin.KeyBindingAccessor;
import de.johni0702.minecraft.gui.function.KeyInput;
import de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.core.events.KeyBindingEventCallback;
import com.replaymod.core.events.KeyEventCallback;
//#if FABRIC>=1
import com.replaymod.core.versions.LangResourcePack;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import static com.replaymod.core.ReplayMod.MOD_ID;
//#else
//$$ import net.minecraftforge.fml.client.registry.ClientRegistry;
//#endif

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class KeyBindingRegistry extends EventRegistrations {
    //#if MC>=12109
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "general"));
    //#else
    //$$ private static final String CATEGORY = "replaymod.title";
    //#if FABRIC>=1 && MC<11600
    //$$ static { net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry.INSTANCE.addCategory(CATEGORY); }
    //#endif

    private final Map<String, Binding> bindings = new HashMap<>();
    private Set<KeyMapping> onlyInReplay = new HashSet<>();
    private Multimap<Integer, Function<KeyInput, Boolean>> rawHandlers = ArrayListMultimap.create();
    private static List<KeyMapping> keybinds = new ArrayList<>();

    public Binding registerKeyBinding(String name, int keyCode, Runnable whenPressed, boolean onlyInRepay) {
        Binding binding = registerKeyBinding(name, keyCode, onlyInRepay);
        binding.handlers.add(whenPressed);
        return binding;
    }

    public Binding registerRepeatedKeyBinding(String name, int keyCode, Runnable whenPressed, boolean onlyInRepay) {
        Binding binding = registerKeyBinding(name, keyCode, onlyInRepay);
        binding.repeatedHandlers.add(whenPressed);
        return binding;
    }

    private Binding registerKeyBinding(String name, int keyCode, boolean onlyInRepay) {
        Binding binding = bindings.get(name);
        if (binding == null) {
            //#if FABRIC>=1
            if (keyCode == 0) {
                keyCode = -1;
            }
            Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name.substring(LangResourcePack.LEGACY_KEY_PREFIX.length()));
            //#if MC>=11600
            String key = String.format("key.%s.%s", id.getNamespace(), id.getPath());
            KeyMapping keyBinding = new KeyMapping(key, InputConstants.Type.KEYSYM, keyCode, CATEGORY);
            //KeyBindingHelper.registerKeyBinding(keyBinding);
            keybinds.add(keyBinding);
            //#else
            //$$ FabricKeyBinding fabricKeyBinding = FabricKeyBinding.Builder.create(id, InputUtil.Type.KEYSYM, keyCode, CATEGORY).build();
            //$$ net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry.INSTANCE.register(fabricKeyBinding);
            //$$ KeyBinding keyBinding = fabricKeyBinding;
            //#endif
            //#else
            //$$ KeyBinding keyBinding = new KeyBinding(name, keyCode, CATEGORY);
            //$$ ClientRegistry.registerKeyBinding(keyBinding);
            //#endif
            binding = new Binding(name, keyBinding);
            bindings.put(name, binding);
            if (onlyInRepay) {
                this.onlyInReplay.add(keyBinding);
            }
        } else if (!onlyInRepay) {
            this.onlyInReplay.remove(binding.keyBinding);
        }
        return binding;
    }

    public void registerRaw(int keyCode, Function<KeyInput, Boolean> whenPressed) {
        rawHandlers.put(keyCode, whenPressed);
    }

    public Map<String, Binding> getBindings() {
        return Collections.unmodifiableMap(bindings);
    }

    public Set<KeyMapping> getOnlyInReplay() {
        return Collections.unmodifiableSet(onlyInReplay);
    }

    { on(PreRenderCallback.EVENT, this::handleRepeatedKeyBindings); }

    public void handleRepeatedKeyBindings() {
        for (Binding binding : bindings.values()) {
            if (binding.keyBinding.isDown()) {
                invokeKeyBindingHandlers(binding, binding.repeatedHandlers);
            }
        }
    }

    { on(KeyBindingEventCallback.EVENT, this::handleKeyBindings); }
    private void handleKeyBindings() {
        for (Binding binding : bindings.values()) {
            while (binding.keyBinding.consumeClick()) {
                invokeKeyBindingHandlers(binding, binding.handlers);
                invokeKeyBindingHandlers(binding, binding.repeatedHandlers);
            }
        }
    }

    private void invokeKeyBindingHandlers(Binding binding, Collection<Runnable> handlers) {
        for (final Runnable runnable : handlers) {
            try {
                runnable.run();
            } catch (Throwable cause) {
                CrashReport crashReport = CrashReport.forThrowable(cause, "Handling Key Binding");
                CrashReportCategory category = crashReport.addCategory("Key Binding");
                category.setDetail("Key Binding", () -> binding.name);
                category.setDetail("Handler", runnable::toString);
                throw new ReportedException(crashReport);
            }
        }
    }

    { on(KeyEventCallback.EVENT, this::handleRaw); }
    private boolean handleRaw(KeyInput keyInput, int action) {
        if (action != KeyEventCallback.ACTION_PRESS) return false;
        for (final Function<KeyInput, Boolean> handler : rawHandlers.get(keyInput.key)) {
            try {
                if (handler.apply(keyInput)) {
                    return true;
                }
            } catch (Throwable cause) {
                CrashReport crashReport = CrashReport.forThrowable(cause, "Handling Raw Key Binding");
                CrashReportCategory category = crashReport.addCategory("Key Binding");
                category.setDetail("Key Code", () -> "" + keyInput.key);
                category.setDetail("Handler", handler::toString);
                throw new ReportedException(crashReport);
            }
        }
        return false;
    }

    public class Binding {
        public final String name;
        public final KeyMapping keyBinding;
        private final List<Runnable> handlers = new ArrayList<>();
        private final List<Runnable> repeatedHandlers = new ArrayList<>();
        private boolean autoActivation;
        private Consumer<Boolean> autoActivationUpdate;

        public Binding(String name, KeyMapping keyBinding) {
            this.name = name;
            this.keyBinding = keyBinding;
        }

        public String getBoundKey() {
            try {
                return keyBinding.getTranslatedKeyMessage().getString();
            } catch (ArrayIndexOutOfBoundsException e) {
                // Apparently windows likes to press strange keys, see https://www.replaymod.com/forum/thread/55
                return "Unknown";
            }
        }

        public boolean isBound() {
            //#if MC>=11400
            return !keyBinding.isUnbound();
            //#else
            //$$ return keyBinding.getKeyCode() != 0;
            //#endif
        }

        public void trigger() {
            KeyBindingAccessor acc = (KeyBindingAccessor) keyBinding;
            acc.setPressTime(acc.getPressTime() + 1);
            handleKeyBindings();
        }

        public void registerAutoActivationSupport(boolean active, Consumer<Boolean> update) {
            this.autoActivation = active;
            this.autoActivationUpdate = update;
        }

        public boolean supportsAutoActivation() {
            return autoActivationUpdate != null;
        }

        public boolean isAutoActivating() {
            return supportsAutoActivation() && autoActivation;
        }

        public void setAutoActivating(boolean active) {
            if (this.autoActivation == active) {
                return;
            }
            this.autoActivation = active;
            this.autoActivationUpdate.accept(active);
        }
    }

    @SubscribeEvent
    public static void registerKeybinds(RegisterKeyMappingsEvent event) {
        for (KeyMapping binding: keybinds) {
            event.register(binding);
        }
    }
}
