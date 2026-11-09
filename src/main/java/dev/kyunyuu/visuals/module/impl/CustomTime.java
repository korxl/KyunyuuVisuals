package dev.kyunyuu.visuals.module.impl;

import dev.kyunyuu.visuals.module.Category;
import dev.kyunyuu.visuals.module.Module;
import dev.kyunyuu.visuals.module.setting.ModeSetting;
import dev.kyunyuu.visuals.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;

public class CustomTime extends Module {
    public final ModeSetting preset = addSetting(new ModeSetting("Preset", "Noon",
            "Day", "Noon", "Sunset", "Night", "Midnight", "Custom"));
    public final NumberSetting time = addSetting(new NumberSetting("Time", 6000, 0, 23999, 100, "t",
            () -> preset.is("Custom")));

    public CustomTime() {
        super("CustomTime", "Locks the client-side time of day, purely visual.", Category.VISUALS);
    }

    @Override
    protected void onEnable() {
        apply(MinecraftClient.getInstance());
    }

    @Override
    public void onTick(MinecraftClient client) {
        apply(client);
    }

    private void apply(MinecraftClient client) {
        if (client.world == null) return;
        client.world.setTime(client.world.getTime(), timeOfDay(), false);
    }

    private long timeOfDay() {
        return switch (preset.index()) {
            case 0 -> 1000L;
            case 1 -> 6000L;
            case 2 -> 12500L;
            case 3 -> 13000L;
            case 4 -> 18000L;
            default -> (long) time.intValue();
        };
    }
}
