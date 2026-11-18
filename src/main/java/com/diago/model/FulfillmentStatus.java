package com.diago.model;

/** Suivi d'une réservation par l'administrateur (validation, livraison...). */
public enum FulfillmentStatus {
    PENDING("En attente", "pending"),
    CONFIRMED("Validée", "confirmed"),
    IN_DELIVERY("En livraison", "delivery"),
    DELIVERED("Remise au client", "delivered"),
    RETURNED("Terminée", "returned"),
    REJECTED("Refusée", "rejected");

    private final String label, css;
    FulfillmentStatus(String label, String css) { this.label = label; this.css = css; }
    public String getLabel() { return label; }
    public String getCss() { return css; }
}