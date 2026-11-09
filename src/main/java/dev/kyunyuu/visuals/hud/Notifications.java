package dev.kyunyuu.visuals.hud;

import dev.kyunyuu.visuals.client.ClientSettings;
import dev.kyunyuu.visuals.module.Module;
import dev.kyunyuu.visuals.render.Paint;
import dev.kyunyuu.visuals.render.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Small slide-in toasts that confirm toggles and important actions.
 */
public final class Notifications {
    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final long LIFETIME = 2700L;

    private Notifications() {
    }

    public static void module(Module module) {
        if (!ClientSettings.NOTIFICATIONS.enabled()) return;
        push(module.getName(), module.isEnabled() ? "Enabled" : "Disabled",
                module.isEnabled() ? Theme.ACCENT : Theme.TEXT_MUTED);
    }

    public static void push(String title, String detail, int accent) {
        ENTRIES.add(new Entry(title, detail, accent, System.currentTimeMillis()));
        while (ENTRIES.size() > 4) {
            ENTRIES.remove(0);
        }
    }

    public static void render(DrawContext ctx) {
        if (ENTRIES.isEmpty()) return;

        long now = System.currentTimeMillis();
        ENTRIES.removeIf(entry -> now - entry.created() > LIFETIME);

        float screenWidth = ctx.getScaledWindowWidth();
        float offsetY = 8f;

        for (Entry entry : ENTRIES) {
            float age = (now - entry.created()) / 1000f;
            float appear = Math.min(1f, age / 0.16f);
            float fade = age > 2.15f ? Math.max(0f, 1f - (age - 2.15f) / 0.45f) : 1f;
            float alpha = Math.min(appear, fade);

            float width = 158f;
            float height = 30f;
            float slide = (1f - appear * appear) * 28f;
            float x = screenWidth - width - 10f + slide;
            float y = offsetY;

            int a = (int) (255 * alpha);
            if (a > 0) {
                Paint.shadow(ctx, x, y, width, height, 7f, Paint.withAlpha(0x99000000, (int) (130 * alpha)), 4);
                Paint.roundedRectGradient(ctx, x, y, width, height, 7f,
                        Paint.withAlpha(0xF2151820, (int) (240 * alpha)),
                        Paint.withAlpha(0xF20A0C12, (int) (240 * alpha)));
                Paint.stroke(ctx, x, y, width, height, 7f, 1f, Paint.withAlpha(0xFFFFFF, (int) (28 * alpha)));
                Paint.roundedRect(ctx, x + 6f, y + 7f, 2f, height - 14f, 1f,
                        Paint.withAlpha(entry.accent(), a));
                Paint.text(ctx, entry.title(), x + 13f, y + 7f, Paint.withAlpha(Theme.TEXT, a), false);
                Paint.text(ctx, entry.detail(), x + 13f, y + 17f, Paint.withAlpha(Theme.TEXT_MUTED, a), false);
                Paint.roundedRect(ctx, x + 13f, y + height - 5f, (width - 26f) * fade, 1.5f, 1f,
                        Paint.withAlpha(entry.accent(), (int) (180 * alpha)));
            }
            offsetY += height + 6f;
        }
    }

    private record Entry(String title, String detail, int accent, long created) {
    }
}
