package dev.kyunyuu.visuals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.kyunyuu.visuals.KyunyuuVisuals;
import dev.kyunyuu.visuals.client.ClientSettings;
import dev.kyunyuu.visuals.module.HudModule;
import dev.kyunyuu.visuals.module.Module;
import dev.kyunyuu.visuals.module.ModuleManager;
import dev.kyunyuu.visuals.module.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigManager() {
    }

    public static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("kyunyuuvisuals.json");
    }

    public static void load() {
        try {
            Path path = file();
            if (!Files.exists(path)) {
                save();
                return;
            }

            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();

            if (root.has("client") && root.get("client").isJsonObject()) {
                JsonObject client = root.getAsJsonObject("client");
                for (Setting<?> setting : ClientSettings.SETTINGS) {
                    if (client.has(setting.getName())) {
                        setting.fromJson(client.get(setting.getName()));
                    }
                }
                ClientSettings.applyTheme();
            }

            if (root.has("modules") && root.get("modules").isJsonObject()) {
                JsonObject modules = root.getAsJsonObject("modules");
                for (Module module : ModuleManager.all()) {
                    if (!modules.has(module.getName())) continue;
                    JsonObject entry = modules.getAsJsonObject(module.getName());

                    if (entry.has("settings") && entry.get("settings").isJsonObject()) {
                        JsonObject settings = entry.getAsJsonObject("settings");
                        for (Setting<?> setting : module.getSettings()) {
                            if (settings.has(setting.getName())) {
                                setting.fromJson(settings.get(setting.getName()));
                            }
                        }
                    }
                    if (module instanceof HudModule hud) {
                        if (entry.has("x")) hud.x = entry.get("x").getAsFloat();
                        if (entry.has("y")) hud.y = entry.get("y").getAsFloat();
                    }
                    if (entry.has("enabled")) {
                        module.setEnabled(entry.get("enabled").getAsBoolean());
                    }
                }
            }

            KyunyuuVisuals.LOG.info("Loaded config from {}", path.getFileName());
        } catch (Exception exception) {
            KyunyuuVisuals.LOG.error("Could not load the KyunyuuVisuals config", exception);
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();

            JsonObject client = new JsonObject();
            for (Setting<?> setting : ClientSettings.SETTINGS) {
                client.add(setting.getName(), setting.toJson());
            }
            root.add("client", client);

            JsonObject modules = new JsonObject();
            for (Module module : ModuleManager.all()) {
                JsonObject entry = new JsonObject();
                entry.addProperty("enabled", module.isEnabled());
                if (module instanceof HudModule hud) {
                    entry.addProperty("x", hud.x);
                    entry.addProperty("y", hud.y);
                }
                JsonObject settings = new JsonObject();
                for (Setting<?> setting : module.getSettings()) {
                    settings.add(setting.getName(), setting.toJson());
                }
                entry.add("settings", settings);
                modules.add(module.getName(), entry);
            }
            root.add("modules", modules);

            Path path = file();
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(root));
        } catch (Exception exception) {
            KyunyuuVisuals.LOG.error("Could not save the KyunyuuVisuals config", exception);
        }
    }
}
