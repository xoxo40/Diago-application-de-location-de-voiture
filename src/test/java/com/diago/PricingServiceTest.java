package com.diago;

import com.diago.model.*;
import com.diago.service.PriceBreakdown;
import com.diago.service.PricingService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {

    private final PricingService pricing = new PricingService();

    private Car rav4() {
        return new Car("Toyota", "RAV4", Category.PREMIUM, CarType.SUV, 150_000, 5,
                Transmission.AUTOMATIQUE, Fuel.ESSENCE, true, 4, true, List.of("x"), 4.8, 41, "test");
    }

    @Test
    void sameDayCountsAsOneDay() {
        LocalDate d = LocalDate.of(2026, 12, 20);
        assertEquals(1, PricingService.days(d, d));
    }

    @Test
    void withoutOptions() {
        PriceBreakdown p = pricing.compute(rav4(), LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 23),
                RentalMode.WITHOUT_DRIVER, false, false, false, false, false);
        assertEquals(3, p.days());
        assertEquals(450_000, p.total());
    }

    @Test
    void withDriverAndOptions() {
        // 3 j : 450 000 + chauffeur 150 000 + assurance 45 000 + GPS 5 000 + livraison 20 000
        PriceBreakdown p = pricing.compute(rav4(), LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 23),
                RentalMode.WITH_DRIVER, true, true, false, true, false);
        assertEquals(670_000, p.total());
    }
}
