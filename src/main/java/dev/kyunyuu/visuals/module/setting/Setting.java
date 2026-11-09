package dev.kyunyuu.visuals.module.setting;

import com.google.gson.JsonElement;

import java.util.function.BooleanSupplier;

/**
 * Base class of every configurable value. Settings are serialised to JSON by the config manager
 * and rendered automatically inside the click GUI.
 */
public abstract class Setting<T> {
    private final String name;
    protected T value;
    private final BooleanSupplier visibleWhen;

    protected Setting(String name, T defaultValue) {
        this(name, defaultValue, () -> true);
    }

    protected Setting(String name, T defaultValue, BooleanSupplier visibleWhen) {
        this.name = name;
        this.value = defaultValue;
        this.visibleWhen = visibleWhen;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        onChanged();
    }

    protected void onChanged() {
    }

    public boolean isVisible() {
        return visibleWhen.getAsBoolean();
    }

    /** Text shown on the right side of the setting row. */
    public abstract String getDisplayValue();

    public abstract JsonElement toJson();

    public abstract void fromJson(JsonElement json);
}
