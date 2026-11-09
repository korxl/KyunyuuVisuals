package dev.kyunyuu.visuals.module.impl;

import dev.kyunyuu.visuals.hud.HudRender;
import dev.kyunyuu.visuals.module.HudModule;
import dev.kyunyuu.visuals.render.Paint;
import dev.kyunyuu.visuals.render.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class WatermarkHud extends HudModule {
    public WatermarkHud() {
        super("Watermark", "Minimal client brand in the corner of your screen.", 6f, 6f);
    }

    @Override
    public float width(MinecraftClient client) {
        return 20 + Paint.textWidth("Kyunyuu") + Paint.textWidth("Visuals") + 6;
    }

    @Override
    public float height(MinecraftClient client) {
        return 18f;
    }

    @Override
    public void render(DrawContext ctx, float x, float y, MinecraftClient client) {
        float w = width(client);
        HudRender.card(ctx, x, y, w, 18f);

        float pulse = 0.6f + 0.4f * (float) Math.sin((System.currentTimeMillis() % 2600L) / 2600f * Math.PI * 2f);
        Paint.circle(ctx, x + 12f, y + 9f, 2.6f, Paint.withAlpha(Theme.ACCENT, 120 + Math.round(115 * pulse)));

        float textY = y + (18f - Paint.fontHeight()) / 2f + 1f;
        Paint.text(ctx, "Kyunyuu", x + 19f, textY, Theme.TEXT, HudRender.shadow());
        Paint.text(ctx, "Visuals", x + 19f + Paint.textWidth("Kyunyuu"), textY, Theme.ACCENT, HudRender.shadow());
    }
}
