package dev.kyunyuu.visuals.module.impl;

import dev.kyunyuu.visuals.module.Category;
import dev.kyunyuu.visuals.module.Module;
import dev.kyunyuu.visuals.module.ModuleManager;
import dev.kyunyuu.visuals.module.setting.NumberSetting;

public class Fullbright extends Module {
    public final NumberSetting strength = addSetting(new NumberSetting("Strength", 6.0, 1.0, 16.0, 0.5, "x"));

    public Fullbright() {
        super("Fullbright", "Brightens the lightmap so you can see in caves and at night.", Category.VISUALS);
    }

    /** Gamma value forced into the lightmap update; read by the mixin. */
    public static float gamma() {
        Fullbright module = ModuleManager.get(Fullbright.class);
        return module == null ? 0f : module.strength.floatValue();
    }

    public static boolean active() {
        return ModuleManager.isEnabled(Fullbright.class);
    }
}
