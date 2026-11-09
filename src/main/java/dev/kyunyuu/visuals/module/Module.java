package dev.kyunyuu.visuals.module;

import dev.kyunyuu.visuals.client.ClientSettings;
import dev.kyunyuu.visuals.module.setting.KeybindSetting;
import dev.kyunyuu.visuals.module.setting.Setting;
import dev.kyunyuu.visuals.util.Anim;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

/**
 * A toggleable feature of the client. Every module automatically owns a bind setting and
 * is rendered inside the click GUI without any extra registration.
 */
public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private final KeybindSetting bind;
    private boolean enabled;

    /** UI animation state (0 = off, 1 = on). */
    public final Anim toggleAnim = new Anim(0f, 24f);
    public final Anim hoverAnim = new Anim(0f, 22f);

    protected Module(String name, String description, Category category) {
        this(name, description, category, KeybindSetting.NONE);
    }

    protected Module(String name, String description, Category category, int defaultBind) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.bind = addSetting(new KeybindSetting("Bind", defaultBind));
    }

    protected <T extends Setting<?>> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    public void setEnabled(boolean state) {
        if (this.enabled == state) return;
        this.enabled = state;
        toggleAnim.target(state ? 1f : 0f);
        if (!ClientSettings.ANIMATIONS.enabled()) {
            toggleAnim.snap(state ? 1f : 0f);
        }
        if (state) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    /** Restores a state without calling callbacks (used by the config loader). */
    public void applyLoadedState(boolean state) {
        this.enabled = state;
        this.toggleAnim.snap(state ? 1f : 0f);
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void onTick(MinecraftClient client) {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    public KeybindSetting getBind() {
        return bind;
    }
}
