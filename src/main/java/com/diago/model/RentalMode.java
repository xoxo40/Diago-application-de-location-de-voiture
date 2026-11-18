package com.diago.model;

public enum RentalMode {
    WITH_DRIVER("Avec chauffeur", "🧑‍✈️"),
    WITHOUT_DRIVER("Sans chauffeur", "🔑");

    private final String label, icon;
    RentalMode(String label, String icon) { this.label = label; this.icon = icon; }
    public String getLabel() { return label; }
    public String getIcon() { return icon; }
}
