package dev.kyunyuu.visuals.module.impl;

import dev.kyunyuu.visuals.hud.HudRender;
import dev.kyunyuu.visuals.module.HudModule;
import dev.kyunyuu.visuals.render.Paint;
import dev.kyunyuu.visuals.render.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class FpsHud extends HudModule {
    public FpsHud() {
        super("Fps", "Shows your current frame rate.", 6f, 28f);
    }

    @Override
    public float width(MinecraftClient client) {
        return 52f;
    }

    @Override
    public float height(MinecraftClient client) {
        return 18f;
    }

    @Override
    public void render(DrawContext ctx, float x, float y, MinecraftClient client) {
        float width = width(client);
        int fps = client.getCurrentFps();
        int color = fps >= 120 ? Theme.SUCCESS : fps >= 60 ? Theme.TEXT : Theme.WARNING;

        HudRender.card(ctx, x, y, width, 18f);
        Paint.text(ctx, "FPS", x + 8f, y + 5f, Theme.TEXT_DIM, HudRender.shadow());
        Paint.textRight(ctx, String.valueOf(fps), x + width - 8f, y + 5f, color, HudRender.shadow());
    }
}
