package dev.kyunyuu.visuals.module.impl;

import dev.kyunyuu.visuals.hud.HudRender;
import dev.kyunyuu.visuals.module.HudModule;
import dev.kyunyuu.visuals.render.Paint;
import dev.kyunyuu.visuals.render.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

public class CpsHud extends HudModule {
    private final Deque<Long> leftClicks = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();
    private boolean leftDown;
    private boolean rightDown;

    public CpsHud() {
        super("Cps", "Clicks per second of both mouse buttons.", 6f, 50f);
    }

    @Override
    public float width(MinecraftClient client) {
        return 64f;
    }

    @Override
    public float height(MinecraftClient client) {
        return 18f;
    }

    @Override
    public void render(DrawContext ctx, float x, float y, MinecraftClient client) {
        poll(client);
        float width = width(client);
        HudRender.card(ctx, x, y, width, 18f);
        Paint.text(ctx, "CPS", x + 8f, y + 5f, Theme.TEXT_DIM, HudRender.shadow());
        Paint.textRight(ctx, leftClicks.size() + " | " + rightClicks.size(), x + width - 8f, y + 5f,
                Theme.TEXT, HudRender.shadow());
    }

    private void poll(MinecraftClient client) {
        long now = System.currentTimeMillis();
        if (client.currentScreen == null) {
            long handle = client.getWindow().getHandle();
            boolean left = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            boolean right = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
            if (left && !leftDown) leftClicks.addLast(now);
            if (right && !rightDown) rightClicks.addLast(now);
            leftDown = left;
            rightDown = right;
        }
        prune(leftClicks, now);
        prune(rightClicks, now);
    }

    private void prune(Deque<Long> clicks, long now) {
        while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000L) {
            clicks.pollFirst();
        }
    }
}
