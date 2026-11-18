package com.diago.model;

public enum Transmission {
    MANUELLE("Manuelle"), AUTOMATIQUE("Automatique");

    private final String label;
    Transmission(String label) { this.label = label; }
    public String getLabel() { return label; }
}
