package com.diago.model;

public enum PaymentMethod {
    MOBILE_MONEY("Mobile Money", "📱", "MVola, Orange Money ou Airtel Money"),
    CARD("Carte bancaire", "💳", "Visa ou Mastercard"),
    ON_PICKUP("Paiement à la prise en charge", "💵", "Payez en espèces ou par mobile à la remise des clés");

    private final String label, icon, description;
    PaymentMethod(String label, String icon, String description) {
        this.label = label; this.icon = icon; this.description = description;
    }
    public String getLabel() { return label; }
    public String getIcon() { return icon; }
    public String getDescription() { return description; }
}
