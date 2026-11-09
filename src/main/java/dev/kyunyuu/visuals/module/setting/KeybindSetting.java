package dev.kyunyuu.visuals.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.util.InputUtil;

public class KeybindSetting extends Setting<Integer> {
    public static final int NONE = -1;

    public KeybindSetting(String name, int keyCode) {
        super(name, keyCode);
    }

    public int keyCode() {
        return value;
    }

    public boolean isBound() {
        return value != NONE;
    }

    public boolean isPressed(long windowHandle) {
        return isBound() && InputUtil.isKeyPressed(windowHandle, value);
    }

    @Override
    public String getDisplayValue() {
        if (!isBound()) return "None";
        try {
            return InputUtil.fromKeyCode(value, 0).getLocalizedText().getString().toUpperCase(java.util.Locale.ROOT);
        } catch (Exception exception) {
            return "Key " + value;
        }
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            value = json.getAsInt();
        }
    }
}
