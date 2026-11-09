package dev.kyunyuu.visuals.module.impl;

import dev.kyunyuu.visuals.hud.HudRender;
import dev.kyunyuu.visuals.module.HudModule;
import dev.kyunyuu.visuals.render.Paint;
import dev.kyunyuu.visuals.render.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;

public class PingHud extends HudModule {
    public PingHud() {
        super("Ping", "Latency to the current server.", 6f, 104f);
    }

    @Override
    public float width(MinecraftClient client) {
        return 58f;
    }

    @Override
    public float height(MinecraftClient client) {
        return 18f;
    }

    @Override
    public void render(DrawContext ctx, float x, float y, MinecraftClient client) {
        int ping = -1;
        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        if (handler != null && client.player != null) {
            var entry = handler.getPlayerListEntry(client.player.getUuid());
            if (entry != null) ping = entry.getLatency();
        }

        float width = width(client);
        int color = ping < 0 ? Theme.TEXT_MUTED
                : ping < 80 ? Theme.SUCCESS
                : ping < 160 ? Theme.WARNING
                : Theme.DANGER;
        String value = ping < 0 ? "--" : ping + "ms";

        HudRender.card(ctx, x, y, width, 18f);
        Paint.text(ctx, "PING", x + 8f, y + 5f, Theme.TEXT_DIM, HudRender.shadow());
        Paint.textRight(ctx, value, x + width - 8f, y + 5f, color, HudRender.shadow());
    }
}
