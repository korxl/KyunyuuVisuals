package dev.kyunyuu.visuals.mixin;

import dev.kyunyuu.visuals.module.impl.Fullbright;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Feeds an artificially high gamma value into the lightmap texture update.
 * The vanilla gamma option itself is never modified, so disabling the module
 * instantly restores the original lighting.
 */
@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {

    @Redirect(method = "update",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;"))
    private Object kyunyuu$fullbright(SimpleOption<Double> option) {
        Object value = option.getValue();
        if (!Fullbright.active()) return value;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null && option == client.options.getGamma() && value instanceof Double gamma) {
            return Math.max(gamma, Fullbright.gamma());
        }
        return value;
    }
}
