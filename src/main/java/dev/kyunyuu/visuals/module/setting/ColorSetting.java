package dev.kyunyuu.visuals.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.kyunyuu.visuals.render.Theme;

public class ColorSetting extends Setting<Float> {
    public ColorSetting(String name, float hue) {
        super(name, hue);
    }

    public float hue() {
        return value;
    }

    public int color() {
        return color(255);
    }

    public int color(int alpha) {
        return (Theme.hsv(value, 0.68f, 0.98f) & 0x00FFFFFF) | (alpha << 24);
    }

    public int secondary() {
        return Theme.hsv((value + 0.11f) % 1f, 0.62f, 1f);
    }

    @Override
    public String getDisplayValue() {
        return Math.round(value * 360f) + "\u00B0";
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            value = json.getAsFloat();
        }
    }
}
