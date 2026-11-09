package dev.kyunyuu.visuals;

import dev.kyunyuu.visuals.client.ClientSettings;
import dev.kyunyuu.visuals.config.ConfigManager;
import dev.kyunyuu.visuals.gui.ClickGuiScreen;
import dev.kyunyuu.visuals.hud.HudManager;
import dev.kyunyuu.visuals.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KyunyuuVisuals implements ClientModInitializer {
    public static final String NAME = "KyunyuuVisuals";
    public static final String VERSION = "1.0.0";
    public static final Logger LOG = LoggerFactory.getLogger(NAME);

    public static KeyBinding openGui;

    @Override
    public void onInitializeClient() {
        ClientSettings.applyTheme();
        ModuleManager.init();
        ConfigManager.load();

        openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.kyunyuuvisuals.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.kyunyuuvisuals"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGui.wasPressed()) {
                client.setScreen(new ClickGuiScreen());
            }
            ModuleManager.onTick(client);
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> HudManager.render(context));
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ConfigManager.save());

        LOG.info("{} v{} initialised - have fun!", NAME, VERSION);
    }
}
