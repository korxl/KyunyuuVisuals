package dev.kyunyuu.visuals.module;

import dev.kyunyuu.visuals.hud.Notifications;
import dev.kyunyuu.visuals.module.impl.ArmorHud;
import dev.kyunyuu.visuals.module.impl.ClockHud;
import dev.kyunyuu.visuals.module.impl.CoordsHud;
import dev.kyunyuu.visuals.module.impl.CpsHud;
import dev.kyunyuu.visuals.module.impl.CustomTime;
import dev.kyunyuu.visuals.module.impl.FpsHud;
import dev.kyunyuu.visuals.module.impl.Fullbright;
import dev.kyunyuu.visuals.module.impl.KeystrokesHud;
import dev.kyunyuu.visuals.module.impl.MemoryHud;
import dev.kyunyuu.visuals.module.impl.NoBob;
import dev.kyunyuu.visuals.module.impl.NoHurtCam;
import dev.kyunyuu.visuals.module.impl.PingHud;
import dev.kyunyuu.visuals.module.impl.WatermarkHud;
import dev.kyunyuu.visuals.module.impl.ZoomModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();
    private static final Map<Module, Boolean> BIND_STATE = new HashMap<>();
    private static boolean initialized;

    private ModuleManager() {
    }

    public static void init() {
        if (initialized) return;
        initialized = true;

        add(new Fullbright());
        add(new NoHurtCam());
        add(new NoBob());
        add(new CustomTime());
        add(new ZoomModule());

        add(new WatermarkHud());
        add(new FpsHud());
        add(new CpsHud());
        add(new CoordsHud());
        add(new PingHud());
        add(new KeystrokesHud());
        add(new ArmorHud());
        add(new ClockHud());
        add(new MemoryHud());

        // A pleasant default layout: brand, performance and player info on the left.
        for (Module module : MODULES) {
            if (module instanceof WatermarkHud || module instanceof FpsHud || module instanceof CpsHud
                    || module instanceof CoordsHud || module instanceof PingHud || module instanceof KeystrokesHud) {
                module.applyLoadedState(true);
            }
        }
    }

    private static void add(Module module) {
        MODULES.add(module);
        BIND_STATE.put(module, false);
    }

    public static List<Module> all() {
        return Collections.unmodifiableList(MODULES);
    }

    public static List<Module> byCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : MODULES) {
            if (module.getCategory() == category) result.add(module);
        }
        return result;
    }

    public static Module byName(String name) {
        for (Module module : MODULES) {
            if (module.getName().equalsIgnoreCase(name)) return module;
        }
        return null;
    }

    public static <T extends Module> T get(Class<T> type) {
        for (Module module : MODULES) {
            if (type.isInstance(module)) return type.cast(module);
        }
        return null;
    }

    public static boolean isEnabled(Class<? extends Module> type) {
        Module module = get(type);
        return module != null && module.isEnabled();
    }

    public static void onTick(MinecraftClient client) {
        long window = client.getWindow().getHandle();
        boolean screenOpen = client.currentScreen != null;

        for (Module module : MODULES) {
            if (module.isEnabled()) {
                module.onTick(client);
            }
            boolean down = !screenOpen
                    && module.getBind().isBound()
                    && InputUtil.isKeyPressed(window, module.getBind().keyCode());
            boolean was = BIND_STATE.getOrDefault(module, false);
            if (down && !was) {
                module.toggle();
                Notifications.module(module);
            }
            BIND_STATE.put(module, down);
        }
    }
}
