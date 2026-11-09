package dev.kyunyuu.visuals.module.impl;

import dev.kyunyuu.visuals.hud.HudRender;
import dev.kyunyuu.visuals.module.HudModule;
import dev.kyunyuu.visuals.render.Paint;
import dev.kyunyuu.visuals.render.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClockHud extends HudModule {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public ClockHud() {
        super("Clock", "Local time on your computer.", 6f, 126f);
    }

    @Override
    public float width(MinecraftClient client) {
        return 62f;
    }

    @Override
    public float height(MinecraftClient client) {
        return 18f;
    }

    @Override
    public void render(DrawContext ctx, float x, float y, MinecraftClient client) {
        float width = width(client);
        HudRender.card(ctx, x, y, width, 18f);
        Paint.text(ctx, "TIME", x + 8f, y + 5f, Theme.TEXT_DIM, HudRender.shadow());
        Paint.textRight(ctx, LocalTime.now().format(FORMAT), x + width - 8f, y + 5f, Theme.TEXT,
                HudRender.shadow());
    }
}
