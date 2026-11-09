package dev.kyunyuu.visuals.client;

import dev.kyunyuu.visuals.module.setting.BooleanSetting;
import dev.kyunyuu.visuals.module.setting.ColorSetting;
import dev.kyunyuu.visuals.module.setting.NumberSetting;
import dev.kyunyuu.visuals.module.setting.Setting;
import dev.kyunyuu.visuals.render.Theme;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings that belong to the client itself instead of a single module.
 */
public final class ClientSettings {
    public static final List<Setting<?>> SETTINGS = new ArrayList<>();

    public static final BooleanSetting BLUR = add(new BooleanSetting("Menu blur", true));
    public static final BooleanSetting ANIMATIONS = add(new BooleanSetting("Animations", true));
    public static final NumberSetting ANIM_SPEED = add(new NumberSetting("Anim speed", 1.0, 0.5, 2.0, 0.05, "x"));
    public static final ColorSetting ACCENT = add(new ColorSetting("Accent hue", 0.75f) {
        @Override
        protected void onChanged() {
            Theme.setAccentHue(getValue());
        }
    });
    public static final BooleanSetting NOTIFICATIONS = add(new BooleanSetting("Notifications", true));
    public static final BooleanSetting TEXT_SHADOW = add(new BooleanSetting("Text shadows", false));
    public static final BooleanSetting BLUR_GLOW = add(new BooleanSetting("Ambient glow", true));

    private ClientSettings() {
    }

    private static <T extends Setting<?>> T add(T setting) {
        SETTINGS.add(setting);
        return setting;
    }

    public static void applyTheme() {
        Theme.setAccentHue(ACCENT.hue());
    }

    /** Multiplier applied to every UI animation speed. */
    public static float animSpeed() {
        return ANIMATIONS.enabled() ? ANIM_SPEED.floatValue() : 60f;
    }
}
