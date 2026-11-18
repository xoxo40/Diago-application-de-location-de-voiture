package com.diago.model;

public enum Category {
    SIMPLE("Simple", "🚗", "simple", "Économique et pratique"),
    PREMIUM("Premium", "⭐", "premium", "Confort et élégance"),
    VIP("VIP", "👑", "vip", "Le luxe à la malgache");

    private final String label, icon, css, tagline;

    Category(String label, String icon, String css, String tagline) {
        this.label = label; this.icon = icon; this.css = css; this.tagline = tagline;
    }
    public String getLabel() { return label; }
    public String getIcon() { return icon; }
    public String getCss() { return css; }
    public String getTagline() { return tagline; }
}
