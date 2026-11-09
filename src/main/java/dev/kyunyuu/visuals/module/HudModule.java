package dev.kyunyuu.visuals.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * A module that draws itself on the HUD and can be dragged around in the HUD editor.
 */
public abstract class HudModule extends Module {
    public float x;
    public float y;

    protected HudModule(String name, String description, float defaultX, float defaultY) {
        super(name, description, Category.HUD);
        this.x = defaultX;
        this.y = defaultY;
    }

    public abstract float width(MinecraftClient client);

    public abstract float height(MinecraftClient client);

    public abstract void render(DrawContext ctx, float x, float y, MinecraftClient client);

    public final void renderAt(DrawContext ctx, MinecraftClient client) {
        render(ctx, x, y, client);
    }
}
