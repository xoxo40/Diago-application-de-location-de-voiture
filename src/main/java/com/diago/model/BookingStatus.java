package com.diago.model;

public enum BookingStatus {
    UPCOMING("À venir", "upcoming"),
    ACTIVE("En cours", "active"),
    COMPLETED("Terminée", "completed"),
    CANCELLED("Annulée", "cancelled");

    private final String label, css;
    BookingStatus(String label, String css) { this.label = label; this.css = css; }
    public String getLabel() { return label; }
    public String getCss() { return css; }
}
