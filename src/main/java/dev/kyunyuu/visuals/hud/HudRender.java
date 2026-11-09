package dev.kyunyuu.visuals.hud;

import dev.kyunyuu.visuals.client.ClientSettings;
import dev.kyunyuu.visuals.render.Paint;
import net.minecraft.client.gui.DrawContext;

/** Shared glass styling for HUD elements. */
public final class HudRender {
    public static final int RADIUS = 7;

    private HudRender() {
    }

    public static void card(DrawContext ctx, float x, float y, float w, float h) {
        Paint.shadow(ctx, x, y, w, h, RADIUS, 0x55000000, 4);
        Paint.roundedRectGradient(ctx, x, y, w, h, RADIUS, 0xE8141720, 0xE60A0C12);
        Paint.stroke(ctx, x, y, w, h, RADIUS, 1f, 0x1AFFFFFF);
    }

    public static void accentBar(DrawContext ctx, float x, float y, float height) {
        Paint.roundedRect(ctx, x, y, 2f, height, 1f, 0xCCFFFFFF);
    }

    public static void label(DrawContext ctx, String text, float x, float y) {
        Paint.text(ctx, text, x, y, dev.kyunyuu.visuals.render.Theme.TEXT_DIM, shadow());
    }

    public static void value(DrawContext ctx, String text, float x, float y, int color) {
        Paint.text(ctx, text, x, y, color, shadow());
    }

    public static boolean shadow() {
        return ClientSettings.TEXT_SHADOW.enabled();
    }
}
