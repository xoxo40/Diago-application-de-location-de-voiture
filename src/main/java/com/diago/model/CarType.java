package com.diago.model;

public enum CarType {
    BERLINE("Berline"), SUV("SUV"), CROSSOVER("Crossover");

    private final String label;
    CarType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
