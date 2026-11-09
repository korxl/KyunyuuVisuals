package dev.kyunyuu.visuals.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.function.BooleanSupplier;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public BooleanSetting(String name, boolean defaultValue, BooleanSupplier visibleWhen) {
        super(name, defaultValue, visibleWhen);
    }

    public boolean enabled() {
        return value;
    }

    public void toggle() {
        setValue(!value);
    }

    @Override
    public String getDisplayValue() {
        return value ? "On" : "Off";
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            value = json.getAsBoolean();
        }
    }
}
