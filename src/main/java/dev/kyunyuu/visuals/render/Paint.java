package dev.kyunyuu.visuals.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * The drawing toolkit of KyunyuuVisuals.
 *
 * <p>Everything is built from {@link DrawContext#fill(int, int, int, int, int)} scanlines so it works
 * on every renderer (including Sodium/Iris) and never touches pipelines or shaders.
 * Rounded rectangles are rasterised row by row with analytic corner coverage which gives
 * clean anti-aliased edges at any radius.</p>
 */
public final class Paint {
    private Paint() {
    }

    // ------------------------------------------------------------------ shapes

    public static void rect(DrawContext ctx, float x, float y, float w, float h, int color) {
        if (w <= 0f || h <= 0f || alpha(color) == 0) return;
        ctx.fill(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h), color);
    }

    public static void roundedRect(DrawContext ctx, float x, float y, float w, float h, float radius, int color) {
        if (w <= 0.6f || h <= 0.6f || alpha(color) == 0) return;
        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = Math.max(1, Math.round(w));
        int ih = Math.max(1, Math.round(h));
        int r = Math.round(Math.min(radius, Math.min(iw, ih) * 0.5f));

        if (r <= 0) {
            ctx.fill(ix, iy, ix + iw, iy + ih, color);
            return;
        }

        // Solid middle and straight bands.
        ctx.fill(ix, iy + r, ix + iw, iy + ih - r, color);
        ctx.fill(ix + r, iy, ix + iw - r, iy + r, color);
        ctx.fill(ix + r, iy + ih - r, ix + iw - r, iy + ih, color);

        // Rounded corner rows.
        for (int row = 0; row < r; row++) {
            float inset = cornerInset(row + 0.5f, r);
            rowAA(ctx, ix, iy + row, iw, inset, color);
            rowAA(ctx, ix, iy + ih - 1 - row, iw, inset, color);
        }
    }

    public static void roundedRectGradient(DrawContext ctx, float x, float y, float w, float h, float radius,
                                           int top, int bottom) {
        if (w <= 0.6f || h <= 0.6f) return;
        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = Math.max(1, Math.round(w));
        int ih = Math.max(1, Math.round(h));
        int r = Math.round(Math.min(radius, Math.min(iw, ih) * 0.5f));

        if (r <= 0) {
            ctx.fillGradient(ix, iy, ix + iw, iy + ih, top, bottom);
            return;
        }

        ctx.fillGradient(ix, iy + r, ix + iw, iy + ih - r, mix(top, bottom, r / (float) ih),
                mix(top, bottom, (ih - r) / (float) ih));
        for (int row = 0; row < r; row++) {
            int topColor = mix(top, bottom, row / (float) ih);
            int bottomColor = mix(top, bottom, (ih - 1 - row) / (float) ih);
            float inset = cornerInset(row + 0.5f, r);
            rowAA(ctx, ix, iy + row, iw, inset, topColor);
            rowAA(ctx, ix, iy + ih - 1 - row, iw, inset, bottomColor);
        }
    }

    public static void stroke(DrawContext ctx, float x, float y, float w, float h, float radius, float thickness,
                              int color) {
        if (w <= 1f || h <= 1f || alpha(color) == 0) return;
        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = Math.max(1, Math.round(w));
        int ih = Math.max(1, Math.round(h));
        int t = Math.max(1, Math.round(thickness));
        int r = Math.round(Math.min(radius, Math.min(iw, ih) * 0.5f));

        // Straight edges.
        ctx.fill(ix + r, iy, ix + iw - r, iy + t, color);
        ctx.fill(ix + r, iy + ih - t, ix + iw - r, iy + ih, color);
        ctx.fill(ix, iy + r, ix + t, iy + ih - r, color);
        ctx.fill(ix + iw - t, iy + r, ix + iw, iy + ih - r, color);

        // Corner arcs.
        for (int row = 0; row < r; row++) {
            float inset = cornerInset(row + 0.5f, r);
            int left = ix + Math.round(inset);
            int right = ix + iw - Math.round(inset);
            ctx.fill(left, iy + row, left + t, iy + row + 1, color);
            ctx.fill(right - t, iy + row, right, iy + row + 1, color);
            ctx.fill(left, iy + ih - 1 - row, left + t, iy + ih - row, color);
            ctx.fill(right - t, iy + ih - 1 - row, right, iy + ih - row, color);
        }
    }

    /** Soft drop shadow built from a handful of expanding rounded rectangles. */
    public static void shadow(DrawContext ctx, float x, float y, float w, float h, float radius, int color,
                              int spread) {
        int baseAlpha = alpha(color);
        if (baseAlpha == 0) return;
        for (int i = spread; i >= 1; i--) {
            float k = 1f - (i - 1) / (float) spread;
            int a = (int) (baseAlpha * k * k * 0.45f);
            if (a <= 0) continue;
            roundedRect(ctx, x - i, y - i, w + i * 2f, h + i * 2f, radius + i, withAlpha(color, a));
        }
    }

    /** Soft radial glow, used for the ambient blobs behind the click GUI. */
    public static void glow(DrawContext ctx, float centerX, float centerY, float radius, int color) {
        int r = Math.round(radius);
        int baseAlpha = alpha(color);
        if (r <= 0 || baseAlpha == 0) return;
        for (int dy = -r; dy < r; dy += 2) {
            float y = dy + 1f;
            float half = (float) Math.sqrt(Math.max(0f, (float) r * r - y * y));
            if (half < 0.5f) continue;
            float k = 1f - Math.abs(y) / (float) r;
            int a = (int) (baseAlpha * k * k * 0.5f);
            if (a <= 0) continue;
            ctx.fill(Math.round(centerX - half), Math.round(centerY + dy),
                    Math.round(centerX + half), Math.round(centerY + dy + 2), withAlpha(color, a));
        }
    }

    public static void circle(DrawContext ctx, float centerX, float centerY, float radius, int color) {
        int r = Math.round(radius);
        for (int dy = -r; dy < r; dy++) {
            float y = dy + 0.5f;
            float half = (float) Math.sqrt(Math.max(0f, (float) r * r - y * y));
            if (half < 0.5f) continue;
            ctx.fill(Math.round(centerX - half), Math.round(centerY + dy),
                    Math.round(centerX + half), Math.round(centerY + dy + 1), color);
        }
    }

    public static void ring(DrawContext ctx, float centerX, float centerY, float radius, float thickness, int color) {
        int r = Math.round(radius);
        int inner = Math.round(Math.max(0f, radius - thickness));
        for (int dy = -r; dy < r; dy++) {
            float y = dy + 0.5f;
            float outerHalf = (float) Math.sqrt(Math.max(0f, (float) r * r - y * y));
            float innerHalf = inner > 0 && Math.abs(y) < inner
                    ? (float) Math.sqrt(Math.max(0f, (float) inner * inner - y * y)) : 0f;
            if (outerHalf < 0.5f) continue;
            int left = Math.round(centerX - outerHalf);
            int right = Math.round(centerX + outerHalf);
            if (innerHalf < 0.5f) {
                ctx.fill(left, Math.round(centerY + dy), right, Math.round(centerY + dy + 1), color);
            } else {
                ctx.fill(left, Math.round(centerY + dy), Math.round(centerX - innerHalf),
                        Math.round(centerY + dy + 1), color);
                ctx.fill(Math.round(centerX + innerHalf), Math.round(centerY + dy), right,
                        Math.round(centerY + dy + 1), color);
            }
        }
    }

    // -------------------------------------------------------------------- text

    public static TextRenderer font() {
        return MinecraftClient.getInstance().textRenderer;
    }

    public static int fontHeight() {
        return font().fontHeight;
    }

    public static int textWidth(String text) {
        return font().getWidth(text);
    }

    public static void text(DrawContext ctx, String text, float x, float y, int color, boolean shadow) {
        ctx.drawText(font(), text, Math.round(x), Math.round(y), color, shadow);
    }

    public static void text(DrawContext ctx, String text, float x, float y, int color) {
        text(ctx, text, x, y, color, false);
    }

    public static void textCentered(DrawContext ctx, String text, float centerX, float y, int color, boolean shadow) {
        text(ctx, text, centerX - textWidth(text) / 2f, y, color, shadow);
    }

    public static void textRight(DrawContext ctx, String text, float rightX, float y, int color, boolean shadow) {
        text(ctx, text, rightX - textWidth(text), y, color, shadow);
    }

    /** Truncates text with an ellipsis so it fits into {@code maxWidth}. */
    public static String trim(String text, float maxWidth) {
        if (textWidth(text) <= maxWidth) return text;
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (textWidth(sb.toString() + c + "...") > maxWidth) break;
            sb.append(c);
        }
        return sb + "...";
    }

    // ------------------------------------------------------------------ colour

    public static int alpha(int color) {
        return (color >>> 24) & 0xFF;
    }

    public static int withAlpha(int color, int a) {
        return (color & 0x00FFFFFF) | ((Math.max(0, Math.min(255, a)) & 0xFF) << 24);
    }

    public static int mulAlpha(int color, float factor) {
        return withAlpha(color, Math.round(alpha(color) * Math.max(0f, Math.min(1f, factor))));
    }

    public static int mix(int from, int to, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = Math.round(alpha(from) + (alpha(to) - alpha(from)) * t);
        int r = Math.round(((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t);
        int g = Math.round(((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int rainbow(float offset, float saturation, float brightness) {
        return Theme.hsv((offset % 1f + 1f) % 1f, saturation, brightness);
    }

    // ---------------------------------------------------------------- internals

    private static float cornerInset(float dy, float radius) {
        float d = radius - dy;
        return radius - (float) Math.sqrt(Math.max(0f, radius * radius - d * d));
    }

    private static void rowAA(DrawContext ctx, int x, int y, int width, float inset, int color) {
        int left = x + (int) Math.floor(inset);
        int right = x + width - (int) Math.floor(inset);
        if (right <= left) return;
        float leftFraction = 1f - (inset - (float) Math.floor(inset));
        float rightFraction = leftFraction;

        if (leftFraction < 0.995f) {
            ctx.fill(left, y, left + 1, y + 1, mulAlpha(color, leftFraction));
            left++;
        }
        if (rightFraction < 0.995f) {
            ctx.fill(right - 1, y, right, y + 1, mulAlpha(color, rightFraction));
            right--;
        }
        if (right > left) {
            ctx.fill(left, y, right, y + 1, color);
        }
    }
}
