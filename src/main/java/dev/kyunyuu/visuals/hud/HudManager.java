package dev.kyunyuu.visuals.hud;

import dev.kyunyuu.visuals.module.HudModule;
import dev.kyunyuu.visuals.module.Module;
import dev.kyunyuu.visuals.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class HudManager {
    private HudManager() {
    }

    public static void render(DrawContext ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden || client.player == null) return;

        for (Module module : ModuleManager.all()) {
            if (module instanceof HudModule hud && hud.isEnabled()) {
                hud.renderAt(ctx, client);
            }
        }
        Notifications.render(ctx);
    }
}
