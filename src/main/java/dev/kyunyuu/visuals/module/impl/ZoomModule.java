package dev.kyunyuu.visuals.module.impl;

import dev.kyunyuu.visuals.module.Category;
import dev.kyunyuu.visuals.module.Module;
import dev.kyunyuu.visuals.module.setting.KeybindSetting;
import dev.kyunyuu.visuals.module.setting.NumberSetting;
import dev.kyunyuu.visuals.util.Anim;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ZoomModule extends Module {
    private static ZoomModule instance;

    public final NumberSetting factor = addSetting(new NumberSetting("Factor", 3.0, 1.5, 8.0, 0.5, "x"));
    public final KeybindSetting zoomKey = addSetting(new KeybindSetting("Zoom key", GLFW.GLFW_KEY_C));

    private final Anim zoom = new Anim(1f, 22f);

    public ZoomModule() {
        super("Zoom", "Hold a key for a smooth optical zoom.", Category.VISUALS);
        instance = this;
    }

    @Override
    public void onTick(MinecraftClient client) {
        boolean holding = zoomKey.isBound()
                && InputUtil.isKeyPressed(client.getWindow().getHandle(), zoomKey.keyCode());
        zoom.target(holding ? factor.floatValue() : 1f);
        zoom.update();
    }

    @Override
    protected void onDisable() {
        zoom.snap(1f);
    }

    /** FOV multiplier read by {@code GameRendererMixin}. */
    public static float fovMultiplier() {
        if (instance == null || !instance.isEnabled()) return 1f;
        float value = instance.zoom.peek();
        return value <= 0.01f ? 1f : 1f / value;
    }
}
