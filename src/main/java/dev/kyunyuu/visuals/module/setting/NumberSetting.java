package dev.kyunyuu.visuals.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.function.BooleanSupplier;

public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;
    private final String suffix;

    public NumberSetting(String name, double defaultValue, double min, double max, double step, String suffix) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        this.suffix = suffix;
    }

    public NumberSetting(String name, double defaultValue, double min, double max, double step, String suffix,
                         BooleanSupplier visibleWhen) {
        super(name, defaultValue, visibleWhen);
        this.min = min;
        this.max = max;
        this.step = step;
        this.suffix = suffix;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public double step() {
        return step;
    }

    public float floatValue() {
        return value.floatValue();
    }

    public int intValue() {
        return (int) Math.round(value);
    }

    public float fraction() {
        return (float) ((value - min) / (max - min));
    }

    public void setFromFraction(double fraction) {
        double raw = min + Math.max(0, Math.min(1, fraction)) * (max - min);
        if (step > 0) {
            raw = Math.round(raw / step) * step;
        }
        setValue(Math.max(min, Math.min(max, raw)));
    }

    @Override
    public String getDisplayValue() {
        String number;
        if (step >= 1) {
            number = String.valueOf(intValue());
        } else {
            number = String.format(java.util.Locale.ROOT, "%.2f", value);
            while (number.contains(".") && (number.endsWith("0") || number.endsWith("."))) {
                number = number.substring(0, number.length() - 1);
            }
        }
        return number + suffix;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            value = Math.max(min, Math.min(max, json.getAsDouble()));
        }
    }
}
