package dev.kyunyuu.visuals.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.function.BooleanSupplier;

public class ModeSetting extends Setting<String> {
    private final String[] options;

    public ModeSetting(String name, String defaultValue, String... options) {
        super(name, defaultValue);
        this.options = options;
    }

    public ModeSetting(String name, String defaultValue, BooleanSupplier visibleWhen, String... options) {
        super(name, defaultValue, visibleWhen);
        this.options = options;
    }

    public String[] options() {
        return options;
    }

    public int index() {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(value)) return i;
        }
        return 0;
    }

    public void setIndex(int index) {
        int i = Math.floorMod(index, options.length);
        setValue(options[i]);
    }

    public void cycle(int direction) {
        setIndex(index() + direction);
    }

    public boolean is(String option) {
        return option.equalsIgnoreCase(value);
    }

    @Override
    public String getDisplayValue() {
        return value;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json == null || !json.isJsonPrimitive()) return;
        String loaded = json.getAsString();
        for (String option : options) {
            if (option.equals(loaded)) {
                value = loaded;
                return;
            }
        }
    }
}
