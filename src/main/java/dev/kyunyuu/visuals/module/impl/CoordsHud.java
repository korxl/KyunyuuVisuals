package dev.kyunyuu.visuals.module.impl;

import dev.kyunyuu.visuals.hud.HudRender;
import dev.kyunyuu.visuals.module.HudModule;
import dev.kyunyuu.visuals.render.Paint;
import dev.kyunyuu.visuals.render.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

import java.util.Locale;

public class CoordsHud extends HudModule {
    public CoordsHud() {
        super("Coords", "Your position and the biome you are standing in.", 6f, 72f);
    }

    @Override
    public float width(MinecraftClient client) {
        return 132f;
    }

    @Override
    public float height(MinecraftClient client) {
        return 30f;
    }

    @Override
    public void render(DrawContext ctx, float x, float y, MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        BlockPos pos = client.player.getBlockPos();
        String coords = pos.getX() + " " + pos.getY() + " " + pos.getZ();
        String biome = client.world.getBiome(pos).getKey()
                .map(key -> key.getValue().getPath())
                .orElse("unknown")
                .replace('_', ' ');

        float width = width(client);
        HudRender.card(ctx, x, y, width, 30f);
        Paint.text(ctx, "XYZ", x + 8f, y + 5f, Theme.TEXT_DIM, HudRender.shadow());
        Paint.textRight(ctx, coords, x + width - 8f, y + 5f, Theme.TEXT, HudRender.shadow());
        Paint.text(ctx, Paint.trim(capitalize(biome), width - 16f), x + 8f, y + 17f, Theme.TEXT_MUTED,
                HudRender.shadow());
    }

    private static String capitalize(String text) {
        if (text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1);
    }
}
