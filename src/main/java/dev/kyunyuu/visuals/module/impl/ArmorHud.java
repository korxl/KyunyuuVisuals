package dev.kyunyuu.visuals.module.impl;

import dev.kyunyuu.visuals.hud.HudRender;
import dev.kyunyuu.visuals.module.HudModule;
import dev.kyunyuu.visuals.render.Paint;
import dev.kyunyuu.visuals.render.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class ArmorHud extends HudModule {
    public ArmorHud() {
        super("Armor", "Your four armor pieces with durability bars.", 6f, 170f);
    }

    @Override
    public float width(MinecraftClient client) {
        return 90f;
    }

    @Override
    public float height(MinecraftClient client) {
        return 28f;
    }

    @Override
    public void render(DrawContext ctx, float x, float y, MinecraftClient client) {
        if (client.player == null) return;

        HudRender.card(ctx, x, y, width(client), 28f);
        PlayerInventory inventory = client.player.getInventory();

        for (int i = 0; i < 4; i++) {
            ItemStack stack = inventory.getArmorStack(3 - i);
            float slotX = x + 7f + i * 20f;
            if (stack.isEmpty()) {
                Paint.roundedRect(ctx, slotX, y + 6f, 16f, 16f, 4f, 0x14FFFFFF);
                continue;
            }
            ctx.drawItem(stack, Math.round(slotX), Math.round(y + 6f));
            if (stack.isDamageable()) {
                float fraction = 1f - (stack.getDamage() / (float) stack.getMaxDamage());
                Paint.roundedRect(ctx, slotX, y + 23f, 16f, 2f, 1f, 0x66000000);
                Paint.roundedRect(ctx, slotX, y + 23f, 16f * fraction, 2f, 1f, durabilityColor(fraction));
            }
        }
    }

    private static int durabilityColor(float fraction) {
        if (fraction > 0.6f) return Theme.SUCCESS;
        if (fraction > 0.3f) return Theme.WARNING;
        return Theme.DANGER;
    }
}
