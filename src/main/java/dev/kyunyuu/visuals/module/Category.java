package dev.kyunyuu.visuals.module;

public enum Category {
    VISUALS("Visuals", "visuals"),
    HUD("Hud", "hud"),
    CLIENT("Client", "client");

    private final String label;
    private final String icon;

    Category(String label, String icon) {
        this.label = label;
        this.icon = icon;
    }

    public String label() {
        return label;
    }

    public String icon() {
        return icon;
    }
}
