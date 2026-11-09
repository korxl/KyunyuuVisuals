package dev.kyunyuu.visuals.gui;

import dev.kyunyuu.visuals.config.ConfigManager;
import dev.kyunyuu.visuals.hud.Notifications;
import dev.kyunyuu.visuals.module.HudModule;
import dev.kyunyuu.visuals.module.Module;
import dev.kyunyuu.visuals.module.ModuleManager;
import dev.kyunyuu.visuals.render.Paint;
import dev.kyunyuu.visuals.render.Theme;
import dev.kyunyuu.visuals.util.Anim;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Free-form HUD editor: drag any enabled element, right-click to hide it.
 */
public class HudEditorScreen extends Screen {
    private final Screen parent;
    private final Anim open = new Anim(0f, 13f);

    private HudModule dragging;
    private float offsetX;
    private float offsetY;
    private boolean dirty;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
        this.open.target(1f);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = ctx.getScaledWindowWidth();
        int screenHeight = ctx.getScaledWindowHeight();
        float t = open.get();

        if (client.world != null) {
            applyBlur();
        }
        Paint.rect(ctx, 0, 0, screenWidth, screenHeight, Paint.withAlpha(0x05070B, (int) (130 * t)));

        for (int gx = 0; gx < screenWidth; gx += 16) {
            Paint.rect(ctx, gx, 0, 1f, screenHeight, 0x08FFFFFF);
        }
        for (int gy = 0; gy < screenHeight; gy += 16) {
            Paint.rect(ctx, 0, gy, screenWidth, 1f, 0x08FFFFFF);
        }

        for (Module module : ModuleManager.all()) {
            if (!(module instanceof HudModule hud) || !hud.isEnabled()) continue;
            hud.renderAt(ctx, client);

            float width = hud.width(client);
            float height = hud.height(client);
            boolean over = mouseX >= hud.x - 2f && mouseX <= hud.x + width + 2f
                    && mouseY >= hud.y - 2f && mouseY <= hud.y + height + 2f;
            int outline = hud == dragging ? Theme.ACCENT
                    : over ? Paint.withAlpha(Theme.ACCENT, 170)
                    : 0x1FFFFFFF;
            Paint.stroke(ctx, hud.x - 1f, hud.y - 1f, width + 2f, height + 2f, 7f, 1.2f, outline);
        }

        Paint.rect(ctx, 0, 0, screenWidth, 30f, 0xB30A0C12);
        Paint.rect(ctx, 0, 30f, screenWidth, 1f, 0x14FFFFFF);
        Paint.text(ctx, "HUD Editor", 12f, 10f, Theme.TEXT, true);
        Paint.text(ctx, "Drag to move  \u00B7  right-click to hide  \u00B7  ESC to save", 78f, 11f,
                Theme.TEXT_MUTED, false);

        float buttonWidth = 54f;
        float buttonX = screenWidth - buttonWidth - 10f;
        boolean hovered = mouseX >= buttonX && mouseX <= buttonX + buttonWidth
                && mouseY >= 7f && mouseY <= 23f;
        Paint.roundedRect(ctx, buttonX, 7f, buttonWidth, 16f, 6f,
                hovered ? Paint.withAlpha(Theme.ACCENT, 90) : Paint.withAlpha(Theme.ACCENT, 45));
        Paint.textCentered(ctx, "Done", buttonX + buttonWidth / 2f, 11f, Theme.TEXT, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        if (mouseY >= 7 && mouseY <= 23 && mouseX >= screenWidth - 64 && mouseX <= screenWidth - 10) {
            finish();
            return true;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        for (int i = ModuleManager.all().size() - 1; i >= 0; i--) {
            Module module = ModuleManager.all().get(i);
            if (!(module instanceof HudModule hud) || !hud.isEnabled()) continue;
            float width = hud.width(client);
            float height = hud.height(client);
            if (mouseX >= hud.x && mouseX <= hud.x + width && mouseY >= hud.y && mouseY <= hud.y + height) {
                if (button == 1) {
                    hud.setEnabled(false);
                    dirty = true;
                    return true;
                }
                dragging = hud;
                offsetX = (float) mouseX - hud.x;
                offsetY = (float) mouseY - hud.y;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            float width = dragging.width(client);
            float height = dragging.height(client);
            float maxX = Math.max(0f, client.getWindow().getScaledWidth() - width);
            float maxY = Math.max(0f, client.getWindow().getScaledHeight() - height);
            dragging.x = Math.max(0f, Math.min(maxX, (float) mouseX - offsetX));
            dragging.y = Math.max(0f, Math.min(maxY, (float) mouseY - offsetY));
            dirty = true;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            finish();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void finish() {
        if (dirty) {
            ConfigManager.save();
            Notifications.push("Hud", "Layout saved", Theme.ACCENT);
        }
        MinecraftClient.getInstance().setScreen(parent);
    }
}
