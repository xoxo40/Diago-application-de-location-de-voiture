package com.diago.model;

public enum Fuel {
    ESSENCE("Essence"), DIESEL("Diesel"), HYBRIDE("Hybride");

    private final String label;
    Fuel(String label) { this.label = label; }
    public String getLabel() { return label; }
}
