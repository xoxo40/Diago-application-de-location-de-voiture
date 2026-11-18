package com.diago.service;

import java.util.List;

/** Détail du prix : location + suppléments = total. */
public record PriceBreakdown(int days, long rental, List<PriceLine> extras, long total) {
    public long extrasTotal() { return total - rental; }

    // Getters "JavaBean" pour Thymeleaf (${price.days}, ${price.total}...)
    public int getDays() { return days; }
    public long getRental() { return rental; }
    public List<PriceLine> getExtras() { return extras; }
    public long getTotal() { return total; }
    public long getExtrasTotal() { return extrasTotal(); }
}
