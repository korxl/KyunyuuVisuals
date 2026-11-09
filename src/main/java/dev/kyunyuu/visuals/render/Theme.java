package dev.kyunyuu.visuals.render;

/**
 * Central colour palette + accent handling for the whole client.
 * Accent colours are derived from a single hue so the entire UI stays coherent.
 */
public final class Theme {
    // Accent (changed by the "Accent hue" client setting)
    public static int ACCENT = 0xFF8B5CF6;
    public static int ACCENT_SECONDARY = 0xFF22D3EE;
    public static int ACCENT_SOFT = 0x338B5CF6;

    // Surfaces
    public static int PANEL = 0xF20A0B10;
    public static int PANEL_INNER = 0xB30F1118;
    public static int SIDEBAR = 0x66000000;
    public static int CARD = 0x14FFFFFF;
    public static int CARD_HOVER = 0x22FFFFFF;
    public static int BORDER = 0x14FFFFFF;
    public static int BORDER_STRONG = 0x26FFFFFF;

    // Text
    public static int TEXT = 0xFFF5F6FA;
    public static int TEXT_DIM = 0xFF9DA3B4;
    public static int TEXT_MUTED = 0xFF666C7C;

    // Semantics
    public static int SUCCESS = 0xFF4ADE80;
    public static int WARNING = 0xFFFBBF24;
    public static int DANGER = 0xFFF87171;

    private Theme() {
    }

    public static void setAccentHue(float hue) {
        hue = ((hue % 1f) + 1f) % 1f;
        ACCENT = hsv(hue, 0.68f, 0.98f);
        ACCENT_SECONDARY = hsv((hue + 0.11f) % 1f, 0.62f, 1f);
        ACCENT_SOFT = (ACCENT & 0x00FFFFFF) | 0x33000000;
    }

    public static int hsv(float h, float s, float v) {
        h = ((h % 1f) + 1f) % 1f;
        s = Math.max(0f, Math.min(1f, s));
        v = Math.max(0f, Math.min(1f, v));
        int index = (int) (h * 6f);
        float f = h * 6f - index;
        float p = v * (1f - s);
        float q = v * (1f - f * s);
        float t = v * (1f - (1f - f) * s);
        float r;
        float g;
        float b;
        switch (index % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return 0xFF000000 | (Math.round(r * 255f) << 16) | (Math.round(g * 255f) << 8) | Math.round(b * 255f);
    }
}
