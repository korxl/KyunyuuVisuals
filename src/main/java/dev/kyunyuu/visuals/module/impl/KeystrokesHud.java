package dev.kyunyuu.visuals.module.impl;

import dev.kyunyuu.visuals.hud.HudRender;
import dev.kyunyuu.visuals.module.HudModule;
import dev.kyunyuu.visuals.render.Paint;
import dev.kyunyuu.visuals.render.Theme;
import dev.kyunyuu.visuals.util.Anim;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class KeystrokesHud extends HudModule {
    private static final float SIZE = 18f;
    private static final float GAP = 4f;

    private final Map<Integer, Anim> press = new HashMap<>();

    public KeystrokesHud() {
        super("Keystrokes", "Animated WASD and mouse button display.", 6f, 196f);
    }

    @Override
    public float width(MinecraftClient client) {
        return SIZE * 3f + GAP * 2f;
    }

    @Override
    public float height(MinecraftClient client) {
        return SIZE * 3f + GAP * 2f;
    }

    @Override
    public void render(DrawContext ctx, float x, float y, MinecraftClient client) {
        long handle = client.getWindow().getHandle();
        float row = SIZE + GAP;

        key(ctx, x + row, y, SIZE, SIZE, "W", InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_W), 1000);
        key(ctx, x, y + row, SIZE, SIZE, "A", InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_A), 1001);
        key(ctx, x + row, y + row, SIZE, SIZE, "S", InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_S), 1002);
        key(ctx, x + row * 2f, y + row, SIZE, SIZE, "D", InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_D), 1003);

        float wide = (SIZE * 3f + GAP * 2f - GAP) / 2f;
        key(ctx, x, y + row * 2f, wide, SIZE, "LMB",
                GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS, 1004);
        key(ctx, x + wide + GAP, y + row * 2f, wide, SIZE, "RMB",
                GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS, 1005);
    }

    private void key(DrawContext ctx, float x, float y, float w, float h, String label, boolean down, int id) {
        Anim anim = press.computeIfAbsent(id, key -> new Anim(0f, 32f));
        anim.target(down ? 1f : 0f);
        float p = anim.get();

        int background = Paint.mix(0xB30E1118, Paint.withAlpha(Theme.ACCENT, 215), p);
        int border = Paint.mix(0x14FFFFFF, Paint.withAlpha(Theme.ACCENT, 150), p);
        int text = Paint.mix(Theme.TEXT_DIM, 0xFFFFFFFF, p);

        Paint.roundedRect(ctx, x, y, w, h, 5f, background);
        Paint.stroke(ctx, x, y, w, h, 5f, 1f, border);
        Paint.textCentered(ctx, label, x + w / 2f, y + (h - Paint.fontHeight()) / 2f + 1f, text,
                HudRender.shadow());
    }
}
