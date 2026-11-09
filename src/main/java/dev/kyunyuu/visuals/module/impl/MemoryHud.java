package dev.kyunyuu.visuals.module.impl;

import dev.kyunyuu.visuals.hud.HudRender;
import dev.kyunyuu.visuals.module.HudModule;
import dev.kyunyuu.visuals.render.Paint;
import dev.kyunyuu.visuals.render.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class MemoryHud extends HudModule {
    private static final long MB = 1024L * 1024L;

    public MemoryHud() {
        super("Memory", "Used and allocated JVM heap memory.", 6f, 148f);
    }

    @Override
    public float width(MinecraftClient client) {
        return 96f;
    }

    @Override
    public float height(MinecraftClient client) {
        return 18f;
    }

    @Override
    public void render(DrawContext ctx, float x, float y, MinecraftClient client) {
        Runtime runtime = Runtime.getRuntime();
        long used = (runtime.totalMemory() - runtime.freeMemory()) / MB;
        long max = runtime.maxMemory() / MB;
        float pressure = max <= 0 ? 0f : used / (float) max;
        int color = pressure > 0.85f ? Theme.DANGER : pressure > 0.65f ? Theme.WARNING : Theme.TEXT;

        float width = width(client);
        HudRender.card(ctx, x, y, width, 18f);
        Paint.text(ctx, "RAM", x + 8f, y + 5f, Theme.TEXT_DIM, HudRender.shadow());
        Paint.textRight(ctx, used + "/" + max + " MB", x + width - 8f, y + 5f, color, HudRender.shadow());
    }
}
