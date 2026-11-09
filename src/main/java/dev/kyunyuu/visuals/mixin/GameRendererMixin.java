package dev.kyunyuu.visuals.mixin;

import dev.kyunyuu.visuals.module.ModuleManager;
import dev.kyunyuu.visuals.module.impl.NoHurtCam;
import dev.kyunyuu.visuals.module.impl.ZoomModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void kyunyuu$disableHurtTilt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (ModuleManager.isEnabled(NoHurtCam.class)) {
            ci.cancel();
        }
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void kyunyuu$zoomFov(Camera camera, float tickDelta, boolean changingFov,
                                 CallbackInfoReturnable<Float> cir) {
        float multiplier = ZoomModule.fovMultiplier();
        if (multiplier != 1f) {
            cir.setReturnValue(cir.getReturnValue() * multiplier);
        }
    }
}
