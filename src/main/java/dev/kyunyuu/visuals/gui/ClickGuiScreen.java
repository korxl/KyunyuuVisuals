package dev.kyunyuu.visuals.gui;

import dev.kyunyuu.visuals.KyunyuuVisuals;
import dev.kyunyuu.visuals.client.ClientSettings;
import dev.kyunyuu.visuals.config.ConfigManager;
import dev.kyunyuu.visuals.hud.Notifications;
import dev.kyunyuu.visuals.module.Category;
import dev.kyunyuu.visuals.module.Module;
import dev.kyunyuu.visuals.module.ModuleManager;
import dev.kyunyuu.visuals.module.setting.BooleanSetting;
import dev.kyunyuu.visuals.module.setting.ColorSetting;
import dev.kyunyuu.visuals.module.setting.KeybindSetting;
import dev.kyunyuu.visuals.module.setting.ModeSetting;
import dev.kyunyuu.visuals.module.setting.NumberSetting;
import dev.kyunyuu.visuals.module.setting.Setting;
import dev.kyunyuu.visuals.render.Paint;
import dev.kyunyuu.visuals.render.Theme;
import dev.kyunyuu.visuals.util.Anim;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The KyunyuuVisuals click GUI: a single glass panel with a category rail, a searchable module
 * list and a live settings inspector. Every element animates through {@link Anim}.
 */
public class ClickGuiScreen extends Screen {
    private static final float MAX_WIDTH = 600f;
    private static final float MAX_HEIGHT = 380f;
    private static final float HEADER = 44f;

    private final Anim open;
    private final Anim select;
    private final Map<Category, Anim> tabHover = new HashMap<>();
    private final Map<Object, Anim> anims = new HashMap<>();
    private final List<ClickArea> areas = new ArrayList<>();
    private final List<RowHit> rows = new ArrayList<>();

    private Category category = Category.VISUALS;
    private Module selected;
    private String search = "";
    private boolean searchFocused;

    private float scroll;
    private float scrollTarget;
    private float settingsScroll;
    private float settingsTarget;

    private KeybindSetting listening;
    private NumberSetting draggingSlider;
    private ColorSetting draggingColor;
    private float dragX;
    private float dragWidth;

    private double lastMouseX;
    private double lastMouseY;

    // Layout captured during render for scrolling / hit testing.
    private float listX;
    private float listY;
    private float listWidth;
    private float listBottom;
    private float settingsX;
    private float settingsTop;
    private float settingsWidth;
    private float settingsBottom;

    public ClickGuiScreen() {
        super(Text.literal("KyunyuuVisuals"));
        float speed = ClientSettings.animSpeed();
        this.open = new Anim(0f, 11f * speed);
        this.select = new Anim(1f, 18f * speed);
        this.open.target(1f);

        List<Module> first = ModuleManager.byCategory(Category.VISUALS);
        if (!first.isEmpty()) this.selected = first.get(0);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        ConfigManager.save();
        super.close();
    }

    // ------------------------------------------------------------------- render

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        areas.clear();
        rows.clear();

        float t = open.get();
        float scale = 0.955f + 0.045f * t;
        float panelWidth = Math.min(MAX_WIDTH, ctx.getScaledWindowWidth() - 24f) * scale;
        float panelHeight = Math.min(MAX_HEIGHT, ctx.getScaledWindowHeight() - 24f) * scale;
        float x = (ctx.getScaledWindowWidth() - panelWidth) / 2f;
        float y = (ctx.getScaledWindowHeight() - panelHeight) / 2f;

        // Backdrop: vanilla blur + dark scrim + slow moving accent glows.
        if (client.world != null && ClientSettings.BLUR.enabled()) {
            applyBlur();
        }
        Paint.rect(ctx, 0, 0, ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight(),
                Paint.withAlpha(0x05070B, (int) (150 * t)));

        if (ClientSettings.BLUR_GLOW.enabled()) {
            float time = (System.currentTimeMillis() % 24000L) / 24000f;
            float angle = time * (float) (Math.PI * 2);
            Paint.glow(ctx, x + panelWidth * 0.28f + (float) Math.cos(angle) * 46f,
                    y + panelHeight * 0.18f + (float) Math.sin(angle) * 30f, 180f,
                    Paint.withAlpha(Theme.ACCENT, (int) (68 * t)));
            Paint.glow(ctx, x + panelWidth * 0.82f + (float) Math.cos(angle + 2.1f) * 40f,
                    y + panelHeight * 0.86f, 200f, Paint.withAlpha(Theme.ACCENT_SECONDARY, (int) (58 * t)));
        }

        // Panel.
        Paint.shadow(ctx, x, y, panelWidth, panelHeight, 16f, Paint.withAlpha(0x000000, (int) (160 * t)), 9);
        Paint.roundedRectGradient(ctx, x, y, panelWidth, panelHeight, 16f,
                Paint.withAlpha(0x0D0F15, (int) (250 * t)), Paint.withAlpha(0x07080C, (int) (250 * t)));
        Paint.stroke(ctx, x, y, panelWidth, panelHeight, 16f, 1f,
                Paint.withAlpha(0xFFFFFF, (int) (26 * t)));

        renderHeader(ctx, x, y, panelWidth, t);
        renderSidebar(ctx, x, y, panelWidth, panelHeight, mouseX, mouseY);

        float sidebarWidth = Math.max(104f, panelWidth * 0.21f);
        float contentX = x + sidebarWidth;
        float contentWidth = panelWidth - sidebarWidth;
        float settingsPanelWidth = Math.max(160f, contentWidth * 0.39f);

        listX = contentX + 10f;
        listY = y + HEADER + 4f;
        listWidth = contentWidth - settingsPanelWidth - 28f;
        listBottom = y + panelHeight - 12f;

        settingsX = contentX + contentWidth - settingsPanelWidth - 12f;
        settingsTop = listY;
        settingsWidth = settingsPanelWidth;
        settingsBottom = listBottom;

        renderSearch(ctx);
        if (category == Category.CLIENT) {
            renderClientPanel(ctx, mouseX, mouseY);
        } else {
            renderModuleList(ctx, mouseX, mouseY);
        }
        renderSettingsPanel(ctx, mouseX, mouseY);
    }

    private void renderHeader(DrawContext ctx, float x, float y, float panelWidth, float alpha) {
        float textY = y + 16f;
        Paint.text(ctx, "Kyunyuu", x + 18f, textY, Theme.TEXT, true);
        float brandWidth = Paint.textWidth("Kyunyuu");
        Paint.text(ctx, "Visuals", x + 18f + brandWidth, textY, Theme.ACCENT, true);
        Paint.text(ctx, "v" + KyunyuuVisuals.VERSION, x + 24f + brandWidth + Paint.textWidth("Visuals"),
                textY + 2f, Theme.TEXT_MUTED, false);

        String hint = "RightShift \u00B7 close with ESC";
        Paint.textRight(ctx, hint, x + panelWidth - 44f, textY + 1f,
                Paint.withAlpha(Theme.TEXT_MUTED, (int) (255 * alpha)), false);

        float closeX = x + panelWidth - 30f;
        float closeY = y + 13f;
        boolean hover = lastMouseX >= closeX && lastMouseX <= closeX + 18f
                && lastMouseY >= closeY && lastMouseY <= closeY + 18f;
        Anim hoverAnim = anim(hover ? 1f : 0f, "close");
        hoverAnim.target(hover ? 1f : 0f);
        Paint.roundedRect(ctx, closeX, closeY, 18f, 18f, 6f,
                Paint.mix(0x14FFFFFF, Paint.withAlpha(Theme.DANGER, 120), hoverAnim.get()));
        Paint.textCentered(ctx, "\u00D7", closeX + 9f, closeY + 5f, Theme.TEXT, false);
        areas.add(new ClickArea(closeX, closeY, 18f, 18f, this::close));

        Paint.rect(ctx, x + 16f, y + HEADER - 6f, panelWidth - 32f, 1f, 0x12FFFFFF);
    }

    private void renderSidebar(DrawContext ctx, float x, float y, float panelWidth, float panelHeight,
                               int mouseX, int mouseY) {
        float sidebarWidth = Math.max(104f, panelWidth * 0.21f);
        float itemY = y + HEADER + 6f;

        for (Category value : Category.values()) {
            boolean active = value == category;
            Anim hover = tabHover.computeIfAbsent(value, category -> new Anim(0f, 20f));
            boolean hovered = mouseX >= x + 12f && mouseX <= x + sidebarWidth - 12f
                    && mouseY >= itemY && mouseY <= itemY + 26f;
            hover.target(hovered ? 1f : 0f);
            float hv = hover.get();

            Paint.roundedRect(ctx, x + 12f, itemY, sidebarWidth - 24f, 26f, 8f,
                    active ? Paint.withAlpha(Theme.ACCENT, 40) : Paint.withAlpha(0xFFFFFF, (int) (9 + 11 * hv)));
            if (active) {
                Paint.roundedRect(ctx, x + 12f, itemY + 6f, 2f, 14f, 1f, Theme.ACCENT);
            }
            drawCategoryIcon(ctx, value, x + 22f, itemY + 9f,
                    active ? Theme.ACCENT : Paint.mix(Theme.TEXT_MUTED, Theme.TEXT, hv));
            Paint.text(ctx, value.label(), x + 38f, itemY + 9f,
                    active ? Theme.TEXT : Paint.mix(Theme.TEXT_MUTED, Theme.TEXT, hv), true);

            final Category target = value;
            areas.add(new ClickArea(x + 12f, itemY, sidebarWidth - 24f, 26f, () -> switchCategory(target)));
            itemY += 30f;
        }

        // User card.
        MinecraftClient client = MinecraftClient.getInstance();
        float footerY = y + panelHeight - 48f;
        Paint.roundedRect(ctx, x + 12f, footerY, sidebarWidth - 24f, 36f, 9f, 0x12FFFFFF);
        Paint.circle(ctx, x + 29f, footerY + 18f, 9f, Paint.withAlpha(Theme.ACCENT, 55));
        String name = client.getSession().getUsername();
        String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(Locale.ROOT);
        Paint.textCentered(ctx, initial, x + 29f, footerY + 14f, Theme.TEXT, false);
        Paint.text(ctx, Paint.trim(name, sidebarWidth - 60f), x + 43f, footerY + 9f, Theme.TEXT, true);
        Paint.text(ctx, client.getCurrentFps() + " fps", x + 43f, footerY + 20f, Theme.TEXT_MUTED, false);
    }

    private void drawCategoryIcon(DrawContext ctx, Category category, float x, float y, int color) {
        switch (category) {
            case VISUALS -> {
                Paint.ring(ctx, x + 4f, y + 4f, 4f, 1.3f, color);
                Paint.circle(ctx, x + 4f, y + 4f, 1.3f, color);
            }
            case HUD -> {
                Paint.roundedRect(ctx, x, y + 0.4f, 8f, 1.7f, 0.85f, color);
                Paint.roundedRect(ctx, x, y + 3.2f, 6f, 1.7f, 0.85f, color);
                Paint.roundedRect(ctx, x, y + 6f, 4f, 1.7f, 0.85f, color);
            }
            case CLIENT -> {
                Paint.ring(ctx, x + 4f, y + 4f, 3.6f, 1.4f, color);
                Paint.rect(ctx, x + 3.3f, y - 0.6f, 1.5f, 2.4f, color);
                Paint.rect(ctx, x + 3.3f, y + 6.2f, 1.5f, 2.4f, color);
            }
        }
    }

    private void renderSearch(DrawContext ctx) {
        Paint.roundedRect(ctx, listX, listY, listWidth, 22f, 8f,
                searchFocused ? 0x1FFFFFFF : 0x12FFFFFF);
        Paint.stroke(ctx, listX, listY, listWidth, 22f, 8f, 1f,
                searchFocused ? Paint.withAlpha(Theme.ACCENT, 170) : 0x14FFFFFF);

        Paint.ring(ctx, listX + 11f, listY + 10f, 3.6f, 1.3f, Theme.TEXT_MUTED);
        Paint.rect(ctx, listX + 13.6f, listY + 13.2f, 3.6f, 1.2f, Theme.TEXT_MUTED);

        boolean empty = search.isEmpty();
        String shown = empty && !searchFocused ? "Search modules" : search;
        Paint.text(ctx, Paint.trim(shown, listWidth - 34f), listX + 20f, listY + 7f,
                empty ? Theme.TEXT_MUTED : Theme.TEXT, false);
        if (searchFocused && (System.currentTimeMillis() / 480L) % 2 == 0) {
            float caretX = listX + 21f + Paint.textWidth(search);
            Paint.rect(ctx, caretX, listY + 6f, 1f, 10f, Paint.withAlpha(Theme.ACCENT, 220));
        }
        areas.add(new ClickArea(listX, listY, listWidth, 22f, () -> searchFocused = true));
    }

    private void renderModuleList(DrawContext ctx, int mouseX, int mouseY) {
        List<Module> modules = filteredModules();
        float rowHeight = 28f;
        float gap = 4f;
        float top = listY + 30f;
        float viewport = listBottom - top;

        float contentHeight = modules.size() * (rowHeight + gap);
        scrollTarget = clamp(scrollTarget, 0f, Math.max(0f, contentHeight - viewport));
        scroll += (scrollTarget - scroll) * (ClientSettings.ANIMATIONS.enabled() ? 0.35f : 1f);

        ctx.enableScissor(Math.round(listX), Math.round(top), Math.round(listX + listWidth),
                Math.round(listBottom));

        float rowY = top - scroll;
        for (Module module : modules) {
            boolean visible = rowY + rowHeight >= top && rowY <= listBottom;
            if (visible) {
                renderModuleRow(ctx, module, rowY, rowHeight, mouseX, mouseY, top, listBottom);
            }
            rowY += rowHeight + gap;
        }
        ctx.disableScissor();

        if (modules.isEmpty()) {
            Paint.textCentered(ctx, "Nothing found", listX + listWidth / 2f, top + 12f, Theme.TEXT_MUTED, false);
        }

        drawScrollbar(ctx, listX + listWidth - 3f, top, 3f, viewport, contentHeight, scroll, viewport);
    }

    private void renderModuleRow(DrawContext ctx, Module module, float rowY, float rowHeight,
                                 int mouseX, int mouseY, float top, float bottom) {
        boolean hovered = mouseX >= listX && mouseX <= listX + listWidth
                && mouseY >= Math.max(rowY, top) && mouseY <= Math.min(rowY + rowHeight, bottom);
        Anim hover = anim(module, "row");
        hover.target(hovered ? 1f : 0f);
        float hv = hover.get();

        boolean active = module == selected;
        float enabled = module.toggleAnim.get();

        if (hovered && !active) {
            select(module);
        }

        Paint.roundedRect(ctx, listX, rowY, listWidth, rowHeight, 8f,
                active ? Paint.withAlpha(Theme.ACCENT, 32) : Paint.withAlpha(0xFFFFFF, (int) (7 + 13 * hv)));
        if (active) {
            Paint.roundedRect(ctx, listX, rowY + 6f, 2f, rowHeight - 12f, 1f, Theme.ACCENT);
        }

        Paint.circle(ctx, listX + 14f, rowY + rowHeight / 2f, 2.1f,
                Paint.withAlpha(Theme.ACCENT, (int) (40 + 200 * enabled)));
        Paint.text(ctx, module.getName(), listX + 23f, rowY + 10f,
                Paint.mix(Theme.TEXT_DIM, Theme.TEXT, Math.max(enabled, hv)), true);

        float switchWidth = 26f;
        float switchHeight = 14f;
        float switchX = listX + listWidth - switchWidth - 10f;
        float switchY = rowY + (rowHeight - switchHeight) / 2f;
        drawSwitch(ctx, switchX, switchY, switchWidth, switchHeight, enabled);

        final Module target = module;
        rows.add(new RowHit(target, listX, rowY, listWidth, rowHeight, top, bottom));
        areas.add(new ClickArea(switchX, switchY, switchWidth, switchHeight, () -> {
            target.toggle();
            Notifications.module(target);
        }));
    }

    private void renderSettingsPanel(DrawContext ctx, int mouseX, int mouseY) {
        float height = settingsBottom - settingsTop;
        Paint.roundedRect(ctx, settingsX, settingsTop, settingsWidth, height, 12f, 0x4D000000);
        Paint.stroke(ctx, settingsX, settingsTop, settingsWidth, height, 12f, 1f, 0x10FFFFFF);

        if (category == Category.CLIENT) {
            renderAboutPanel(ctx);
            return;
        }
        if (selected == null) {
            Paint.textCentered(ctx, "Hover a module", settingsX + settingsWidth / 2f,
                    settingsTop + height / 2f - 12f, Theme.TEXT_MUTED, false);
            Paint.textCentered(ctx, "to edit it", settingsX + settingsWidth / 2f,
                    settingsTop + height / 2f, Theme.TEXT_MUTED, false);
            return;
        }

        float top = settingsTop + 12f;
        Paint.text(ctx, selected.getName(), settingsX + 12f, top, Theme.TEXT, true);
        top += 12f;
        Paint.text(ctx, Paint.trim(selected.getDescription(), settingsWidth - 24f), settingsX + 12f, top,
                Theme.TEXT_MUTED, false);
        top += 14f;
        Paint.rect(ctx, settingsX + 12f, top, settingsWidth - 24f, 1f, 0x14FFFFFF);
        top += 8f;

        List<Setting<?>> visible = new ArrayList<>();
        float contentHeight = 0f;
        for (Setting<?> setting : selected.getSettings()) {
            if (!setting.isVisible()) continue;
            visible.add(setting);
            contentHeight += settingHeight(setting) + 6f;
        }

        float viewport = settingsBottom - top - 8f;
        settingsTarget = clamp(settingsTarget, 0f, Math.max(0f, contentHeight - viewport));
        settingsScroll += (settingsTarget - settingsScroll) * (ClientSettings.ANIMATIONS.enabled() ? 0.35f : 1f);

        ctx.enableScissor(Math.round(settingsX), Math.round(top), Math.round(settingsX + settingsWidth),
                Math.round(settingsBottom - 8f));
        float rowY = top - settingsScroll;
        for (Setting<?> setting : visible) {
            float used = drawSetting(ctx, setting, settingsX + 12f, rowY, settingsWidth - 24f);
            rowY += used + 6f;
        }
        ctx.disableScissor();

        drawScrollbar(ctx, settingsX + settingsWidth - 5f, top, 3f, viewport, contentHeight, settingsScroll, viewport);
    }

    private void renderAboutPanel(DrawContext ctx) {
        float top = settingsTop + 14f;
        Paint.text(ctx, "KyunyuuVisuals", settingsX + 12f, top, Theme.ACCENT, true);
        top += 14f;
        Paint.text(ctx, "v" + KyunyuuVisuals.VERSION + " \u00B7 1.21.4", settingsX + 12f, top, Theme.TEXT_DIM, false);
        top += 18f;

        int total = ModuleManager.all().size();
        int enabled = 0;
        for (Module module : ModuleManager.all()) {
            if (module.isEnabled()) enabled++;
        }

        Paint.roundedRect(ctx, settingsX + 12f, top, settingsWidth - 24f, 30f, 8f, 0x14FFFFFF);
        Paint.text(ctx, "Modules", settingsX + 22f, top + 6f, Theme.TEXT_MUTED, false);
        Paint.textRight(ctx, enabled + " / " + total, settingsX + settingsWidth - 22f, top + 6f, Theme.TEXT, true);
        Paint.text(ctx, "Active", settingsX + 22f, top + 16f, Theme.TEXT_MUTED, false);
        top += 38f;

        Paint.text(ctx, "Click a module to toggle it,", settingsX + 12f, top, Theme.TEXT_MUTED, false);
        top += 10f;
        Paint.text(ctx, "hover it to edit its settings.", settingsX + 12f, top, Theme.TEXT_MUTED, false);
        top += 10f;
        Paint.text(ctx, "Right-click a keybind to clear.", settingsX + 12f, top, Theme.TEXT_MUTED, false);
        top += 18f;

        String key = "RightShift";
        Paint.roundedRect(ctx, settingsX + 12f, top, settingsWidth - 24f, 20f, 7f, Paint.withAlpha(Theme.ACCENT, 30));
        Paint.text(ctx, "Open GUI", settingsX + 20f, top + 6f, Theme.TEXT_DIM, false);
        Paint.textRight(ctx, key, settingsX + settingsWidth - 20f, top + 6f, Theme.ACCENT, true);
    }

    private void renderClientPanel(DrawContext ctx, int mouseX, int mouseY) {
        float top = listY + 30f;
        float viewport = listBottom - top;

        List<Setting<?>> visible = new ArrayList<>();
        float contentHeight = 0f;
        for (Setting<?> setting : ClientSettings.SETTINGS) {
            if (!setting.isVisible()) continue;
            visible.add(setting);
            contentHeight += settingHeight(setting) + 6f;
        }
        contentHeight += 74f;

        scrollTarget = clamp(scrollTarget, 0f, Math.max(0f, contentHeight - viewport));
        scroll += (scrollTarget - scroll) * (ClientSettings.ANIMATIONS.enabled() ? 0.35f : 1f);

        ctx.enableScissor(Math.round(listX), Math.round(top), Math.round(listX + listWidth),
                Math.round(listBottom));
        float rowY = top - scroll;
        for (Setting<?> setting : visible) {
            rowY += drawSetting(ctx, setting, listX, rowY, listWidth) + 6f;
        }
        rowY += 4f;
        button(ctx, "Open HUD editor", listX, rowY, listWidth, 22f,
                () -> MinecraftClient.getInstance().setScreen(new HudEditorScreen(this)));
        rowY += 28f;
        button(ctx, "Save config", listX, rowY, listWidth, 22f, ConfigManager::save);
        ctx.disableScissor();

        drawScrollbar(ctx, listX + listWidth - 3f, top, 3f, viewport, contentHeight, scroll, viewport);
    }

    // ------------------------------------------------------------------ widgets

    private float drawSetting(DrawContext ctx, Setting<?> setting, float x, float y, float width) {
        if (setting instanceof BooleanSetting bool) {
            Paint.text(ctx, Paint.trim(bool.getName(), width - 46f), x, y + 3f, Theme.TEXT_DIM, false);
            Anim anim = anim(bool, "value");
            anim.target(bool.enabled() ? 1f : 0f);
            float switchWidth = 26f;
            float switchHeight = 14f;
            float switchX = x + width - switchWidth;
            drawSwitch(ctx, switchX, y + 1f, switchWidth, switchHeight, anim.get());
            areas.add(new ClickArea(switchX, y, switchWidth, 16f, bool::toggle));
            return 18f;
        }

        if (setting instanceof NumberSetting number) {
            Paint.text(ctx, number.getName(), x, y, Theme.TEXT_DIM, false);
            Paint.textRight(ctx, number.getDisplayValue(), x + width, y, Theme.TEXT, false);
            float barY = y + 14f;
            float barHeight = 5f;
            Anim anim = anim(number, "value");
            anim.target(number.fraction());
            float fill = anim.get();

            Paint.roundedRect(ctx, x, barY, width, barHeight, barHeight / 2f, 0x24FFFFFF);
            if (fill > 0.01f) {
                Paint.roundedRectGradient(ctx, x, barY, Math.max(barHeight, width * fill), barHeight,
                        barHeight / 2f, Theme.ACCENT, Theme.ACCENT_SECONDARY);
            }
            Paint.circle(ctx, x + width * fill, barY + barHeight / 2f, 3.6f, 0xFFF5F6FA);
            areas.add(new ClickArea(x, barY - 6f, width, 16f, () -> {
                draggingSlider = number;
                dragX = x;
                dragWidth = width;
                number.setFromFraction((lastMouseX - x) / width);
            }));
            return 24f;
        }

        if (setting instanceof ModeSetting mode) {
            Paint.text(ctx, Paint.trim(mode.getName(), width - 8f), x, y, Theme.TEXT_DIM, false);
            float chipY = y + 13f;
            float chipHeight = 15f;
            Paint.roundedRect(ctx, x, chipY, width, chipHeight, 6f, 0x1FFFFFFF);
            Paint.textCentered(ctx, Paint.trim(mode.getValue(), width - 34f), x + width / 2f, chipY + 4f,
                    Theme.TEXT, false);
            Paint.text(ctx, "\u2039", x + 6f, chipY + 4f, Theme.TEXT_MUTED, false);
            Paint.text(ctx, "\u203A", x + width - 11f, chipY + 4f, Theme.TEXT_MUTED, false);
            areas.add(new ClickArea(x, chipY, width / 2f, chipHeight, () -> mode.cycle(-1)));
            areas.add(new ClickArea(x + width / 2f, chipY, width / 2f, chipHeight, () -> mode.cycle(1)));
            return 30f;
        }

        if (setting instanceof ColorSetting color) {
            Paint.text(ctx, color.getName(), x, y, Theme.TEXT_DIM, false);
            Paint.textRight(ctx, color.getDisplayValue(), x + width, y, Theme.TEXT, false);
            float barY = y + 14f;
            float barHeight = 6f;
            int segments = Math.max(8, Math.round(width / 6f));
            for (int i = 0; i < segments; i++) {
                float segmentX = x + width * i / segments;
                Paint.rect(ctx, segmentX, barY, width / segments + 0.6f, barHeight,
                        Theme.hsv(i / (float) segments, 0.68f, 0.98f));
            }
            Paint.stroke(ctx, x, barY, width, barHeight, 3f, 1f, 0x30FFFFFF);
            Paint.circle(ctx, x + width * color.hue(), barY + barHeight / 2f, 4f, 0xFFFFFFFF);
            areas.add(new ClickArea(x, barY - 6f, width, 16f, () -> {
                draggingColor = color;
                dragX = x;
                dragWidth = width;
                color.setValue(clamp((float) ((lastMouseX - x) / width), 0f, 1f));
            }));
            return 26f;
        }

        if (setting instanceof KeybindSetting keybind) {
            Paint.text(ctx, Paint.trim(keybind.getName(), width - 60f), x, y + 3f, Theme.TEXT_DIM, false);
            boolean listeningThis = listening == keybind;
            float chipWidth = 56f;
            float chipHeight = 15f;
            float chipX = x + width - chipWidth;
            Paint.roundedRect(ctx, chipX, y, chipWidth, chipHeight, 6f,
                    listeningThis ? Paint.withAlpha(Theme.ACCENT, 70) : 0x1FFFFFFF);
            Paint.textCentered(ctx, listeningThis ? "press..." : keybind.getDisplayValue(),
                    chipX + chipWidth / 2f, y + 4f, Theme.TEXT, false);
            areas.add(new ClickArea(chipX, y, chipWidth, chipHeight,
                    () -> listening = listeningThis ? null : keybind));
            return 18f;
        }
        return 0f;
    }

    private void drawSwitch(DrawContext ctx, float x, float y, float width, float height, float progress) {
        Paint.roundedRect(ctx, x, y, width, height, height / 2f,
                Paint.mix(0x2AFFFFFF, Paint.withAlpha(Theme.ACCENT, 240), progress));
        float knob = height - 4f;
        float knobX = x + 2f + (width - knob - 4f) * progress;
        Paint.circle(ctx, knobX + knob / 2f, y + height / 2f, knob / 2f, 0xFFF5F6FA);
    }

    private void button(DrawContext ctx, String label, float x, float y, float width, float height,
                        Runnable action) {
        boolean hovered = lastMouseX >= x && lastMouseX <= x + width
                && lastMouseY >= y && lastMouseY <= y + height;
        Anim hover = anim(label, "button");
        hover.target(hovered ? 1f : 0f);
        float hv = hover.get();

        Paint.roundedRect(ctx, x, y, width, height, 7f,
                Paint.mix(Paint.withAlpha(Theme.ACCENT, 30), Paint.withAlpha(Theme.ACCENT, 70), hv));
        Paint.stroke(ctx, x, y, width, height, 7f, 1f,
                Paint.withAlpha(Theme.ACCENT, (int) (60 + 90 * hv)));
        Paint.textCentered(ctx, label, x + width / 2f, y + (height - Paint.fontHeight()) / 2f + 1f,
                Theme.TEXT, true);
        areas.add(new ClickArea(x, y, width, height, action));
    }

    private void drawScrollbar(DrawContext ctx, float x, float y, float width, float height,
                               float contentHeight, float scroll, float viewport) {
        if (contentHeight <= viewport + 0.5f) return;
        float thumbHeight = Math.max(18f, viewport * (viewport / contentHeight));
        float maxScroll = contentHeight - viewport;
        float progress = maxScroll <= 0f ? 0f : scroll / maxScroll;
        float thumbY = y + (height - thumbHeight) * clamp(progress, 0f, 1f);
        Paint.roundedRect(ctx, x, y, width, height, width / 2f, 0x14FFFFFF);
        Paint.roundedRect(ctx, x, thumbY, width, thumbHeight, width / 2f, Paint.withAlpha(Theme.ACCENT, 150));
    }

    private float settingHeight(Setting<?> setting) {
        if (setting instanceof NumberSetting) return 24f;
        if (setting instanceof ModeSetting) return 30f;
        if (setting instanceof ColorSetting) return 26f;
        return 18f;
    }

    private Anim anim(Object key, String suffix) {
        return anims.computeIfAbsent(key.toString() + ":" + suffix,
                k -> new Anim(0f, 20f * ClientSettings.animSpeed()));
    }

    private List<Module> filteredModules() {
        List<Module> result = new ArrayList<>();
        String query = search.toLowerCase(Locale.ROOT);
        for (Module module : ModuleManager.byCategory(category)) {
            if (query.isEmpty() || module.getName().toLowerCase(Locale.ROOT).contains(query)) {
                result.add(module);
            }
        }
        return result;
    }

    private void select(Module module) {
        if (module == selected) return;
        selected = module;
        settingsScroll = 0f;
        settingsTarget = 0f;
        select.snap(0f);
        select.target(1f);
    }

    private void switchCategory(Category next) {
        if (category == next) return;
        category = next;
        scroll = 0f;
        scrollTarget = 0f;
        settingsScroll = 0f;
        settingsTarget = 0f;
        select.snap(0f);
        select.target(1f);
        if (next != Category.CLIENT) {
            List<Module> modules = ModuleManager.byCategory(next);
            selected = modules.isEmpty() ? null : modules.get(0);
        }
    }

    // -------------------------------------------------------------------- input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        if (listening != null && button == 1) {
            listening.setValue(KeybindSetting.NONE);
            listening = null;
            return true;
        }
        if (listening != null) {
            return true;
        }

        for (int i = areas.size() - 1; i >= 0; i--) {
            ClickArea area = areas.get(i);
            if (area.contains(mouseX, mouseY)) {
                area.action().run();
                return true;
            }
        }

        for (int i = rows.size() - 1; i >= 0; i--) {
            RowHit row = rows.get(i);
            if (row.contains(mouseX, mouseY)) {
                if (button == 0) {
                    row.module().toggle();
                    Notifications.module(row.module());
                } else if (button == 1) {
                    select(row.module());
                }
                return true;
            }
        }

        searchFocused = false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSlider = null;
        draggingColor = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingSlider != null && dragWidth > 0f) {
            draggingSlider.setFromFraction((mouseX - dragX) / dragWidth);
            return true;
        }
        if (draggingColor != null && dragWidth > 0f) {
            draggingColor.setValue(clamp((float) ((mouseX - dragX) / dragWidth), 0f, 1f));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listBottom) {
            scrollTarget = clamp(scrollTarget - (float) vertical * 22f, 0f, Float.MAX_VALUE);
            return true;
        }
        if (mouseX >= settingsX && mouseX <= settingsX + settingsWidth
                && mouseY >= settingsTop && mouseY <= settingsBottom) {
            settingsTarget = clamp(settingsTarget - (float) vertical * 22f, 0f, Float.MAX_VALUE);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening != null) {
            listening.setValue(keyCode == GLFW.GLFW_KEY_ESCAPE ? KeybindSetting.NONE : keyCode);
            listening = null;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!search.isEmpty()) search = search.substring(0, search.length() - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocused && !Character.isISOControl(chr) && search.length() < 24) {
            search += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private record ClickArea(float x, float y, float w, float h, Runnable action) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        }
    }

    private record RowHit(Module module, float x, float y, float w, float h, float viewTop, float viewBottom) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + w
                    && mouseY >= Math.max(y, viewTop) && mouseY <= Math.min(y + h, viewBottom);
        }
    }
}
