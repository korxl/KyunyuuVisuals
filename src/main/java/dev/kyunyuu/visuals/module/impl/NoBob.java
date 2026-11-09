package dev.kyunyuu.visuals.module.impl;

import dev.kyunyuu.visuals.module.Category;
import dev.kyunyuu.visuals.module.Module;
import net.minecraft.client.MinecraftClient;

public class NoBob extends Module {
    private boolean previous = true;

    public NoBob() {
        super("NoBob", "Stops the camera bobbing while walking.", Category.VISUALS);
    }

    @Override
    protected void onEnable() {
        MinecraftClient client = MinecraftClient.getInstance();
        previous = client.options.getBobView().getValue();
        client.options.getBobView().setValue(false);
    }

    @Override
    protected void onDisable() {
        MinecraftClient.getInstance().options.getBobView().setValue(previous);
    }
}
