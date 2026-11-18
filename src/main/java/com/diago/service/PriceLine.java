package com.diago.service;

public record PriceLine(String label, long amount) {
    // Getters "JavaBean" pour Thymeleaf (${l.label}, ${l.amount})
    public String getLabel() { return label; }
    public long getAmount() { return amount; }
}
