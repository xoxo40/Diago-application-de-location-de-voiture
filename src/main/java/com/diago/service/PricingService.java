package com.diago.service;

import com.diago.model.Booking;
import com.diago.model.Car;
import com.diago.model.RentalMode;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Toute la logique de calcul des prix est ici (même règles que la maquette Figma). */
@Service
public class PricingService {

    public static final long DRIVER_PER_DAY = 50_000;
    public static final long INSURANCE_PER_DAY = 15_000;
    public static final long GPS = 5_000;
    public static final long BABY_SEAT = 8_000;
    public static final long DELIVERY = 20_000;
    public static final long RETURN_HOME = 20_000;

    /** Nombre de jours facturés (minimum 1). */
    public static int days(LocalDate start, LocalDate end) {
        return (int) Math.max(1, ChronoUnit.DAYS.between(start, end));
    }

    public PriceBreakdown compute(Car car, LocalDate start, LocalDate end, RentalMode mode,
                                  boolean insurance, boolean gps, boolean babySeat,
                                  boolean delivery, boolean returnHome) {
        int days = days(start, end);
        long rental = car.getPricePerDay() * days;
        long total = rental;
        List<PriceLine> extras = new ArrayList<>();

        if (mode == RentalMode.WITH_DRIVER && car.isDriverAvailable()) {
            extras.add(new PriceLine("Chauffeur professionnel DiaGo", DRIVER_PER_DAY * days));
        }
        if (insurance && car.isInsuranceAvailable()) extras.add(new PriceLine("Assurance tous risques", INSURANCE_PER_DAY * days));
        if (gps && car.isGpsAvailable()) extras.add(new PriceLine("GPS intégré", GPS));
        if (babySeat && car.isBabySeatAvailable()) extras.add(new PriceLine("Siège bébé", BABY_SEAT));
        if (delivery && car.isDeliveryAvailable()) extras.add(new PriceLine("Livraison du véhicule", DELIVERY));
        if (returnHome && car.isReturnHomeAvailable()) extras.add(new PriceLine("Retour à domicile", RETURN_HOME));

        for (PriceLine l : extras) total += l.amount();
        return new PriceBreakdown(days, rental, extras, total);
    }

    public PriceBreakdown compute(Booking b) {
        return compute(b.getCar(), b.getStartDate(), b.getEndDate(), b.getMode(),
                b.isInsurance(), b.isGps(), b.isBabySeat(), b.isDelivery(), b.isReturnHome());
    }
}
