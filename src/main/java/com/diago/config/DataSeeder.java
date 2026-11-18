package com.diago.config;

import com.diago.model.*;
import com.diago.repository.*;
import com.diago.service.PricingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.annotation.Order;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Données de démonstration (chargées une seule fois si la base est vide). */
@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final CarRepository cars;
    private final DriverRepository drivers;
    private final UserRepository users;
    private final BookingRepository bookings;
    private final PasswordEncoder encoder;
    private final PricingService pricing;

    public DataSeeder(CarRepository cars, DriverRepository drivers, UserRepository users,
                      BookingRepository bookings, PasswordEncoder encoder, PricingService pricing) {
        this.cars = cars; this.drivers = drivers; this.users = users;
        this.bookings = bookings; this.encoder = encoder; this.pricing = pricing;
    }

    private static String img(String id) {
        return "https://images.unsplash.com/photo-" + id + "?w=900&h=600&fit=crop&auto=format";
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Compte administrateur : admin@diago.mg / admin1234
        if (!users.existsByEmailIgnoreCase("admin@diago.mg")) {
            User admin = new User("Administrateur DiaGo", "0320000000", "admin@diago.mg", encoder.encode("admin1234"));
            admin.setRole("ADMIN");
            users.save(admin);
        }

        if (cars.count() > 0) return;

        Car corolla = cars.save(new Car("Toyota", "Corolla", Category.SIMPLE, CarType.BERLINE, 45_000, 5,
                Transmission.MANUELLE, Fuel.ESSENCE, true, 2, true,
                List.of(img("1605822167835-d32696aef686"), img("1619431856706-ca2cc58258f6")), 4.3, 18,
                "Berline économique idéale pour les déplacements en ville et sur les routes nationales."));
        Car duster = cars.save(new Car("Renault", "Duster", Category.SIMPLE, CarType.SUV, 55_000, 5,
                Transmission.MANUELLE, Fuel.DIESEL, true, 3, true,
                List.of(img("1658871124533-b558cbb0d58b"), img("1663226894173-ae8f089b28ef")), 4.6, 32,
                "SUV compact robuste, parfait pour les pistes et routes secondaires de Madagascar."));
        Car rav4 = cars.save(new Car("Toyota", "RAV4", Category.PREMIUM, CarType.SUV, 150_000, 5,
                Transmission.AUTOMATIQUE, Fuel.ESSENCE, true, 4, true,
                List.of(img("1760713164476-7eb5063b3d07"), img("1760713170685-b67abc3be5ad")), 4.8, 41,
                "SUV premium avec tout le confort pour vos voyages d'affaires ou en famille."));
        cars.save(new Car("Hyundai", "Tucson", Category.PREMIUM, CarType.CROSSOVER, 130_000, 5,
                Transmission.AUTOMATIQUE, Fuel.DIESEL, true, 4, true,
                List.of(img("1767749995460-933d082f0b4a"), img("1760713164476-7eb5063b3d07")), 4.7, 28,
                "Crossover moderne et confortable pour tous vos déplacements professionnels."));
        Car gle = cars.save(new Car("Mercedes", "GLE 350", Category.VIP, CarType.SUV, 350_000, 5,
                Transmission.AUTOMATIQUE, Fuel.DIESEL, true, 5, true,
                List.of(img("1759505738499-8c9b26c1b7e7"), img("1767749995458-b0927324e4d0")), 5.0, 12,
                "SUV de luxe allemand. Expérience ultime pour vos événements ou voyages d'exception."));
        cars.save(new Car("BMW", "X5", Category.VIP, CarType.SUV, 420_000, 7,
                Transmission.AUTOMATIQUE, Fuel.ESSENCE, true, 5, false,
                List.of(img("1781374824797-ab4fb700fb08"), img("1759505738499-8c9b26c1b7e7")), 4.9, 8,
                "Le summum de l'excellence bavaroise. Confort et prestige incomparables pour 7 passagers."));

        Driver jean = drivers.save(new Driver("Rakoto Jean", 4.9, "+261 34 12 345 67",
                "https://images.unsplash.com/photo-1607642857266-88f5f03e66c6?w=200&h=200&fit=crop&auto=format"));
        Driver michel = drivers.save(new Driver("Andry Michel", 4.8, "+261 33 98 765 43",
                "https://images.unsplash.com/photo-1584041877472-6b1e21552ce8?w=200&h=200&fit=crop&auto=format"));

        // Compte de démonstration : demo@diago.mg / demo1234
        User demo = new User("Aina Rakoto", "0340000000", "demo@diago.mg", encoder.encode("demo1234"));
        demo.getFavorites().add(rav4);
        demo.getFavorites().add(gle);
        demo.getAddresses().add(new SavedAddress(demo, "Domicile", "Antananarivo, Ivandry"));
        demo.getAddresses().add(new SavedAddress(demo, "Travail", "Antananarivo, Antsahavola"));
        users.save(demo);

        LocalDate today = LocalDate.now();
        bookings.save(demoBooking("DGO-0847", demo, rav4, jean, today.plusDays(6), today.plusDays(9),
                "Antananarivo, Ivandry", RentalMode.WITH_DRIVER, true, false, PaymentMethod.MOBILE_MONEY));
        bookings.save(demoBooking("DGO-0631", demo, gle, michel, today.minusDays(1), today.plusDays(1),
                "Aéroport Ivato", RentalMode.WITH_DRIVER, true, true, PaymentMethod.CARD));
        bookings.save(demoBooking("DGO-0512", demo, corolla, null, today.minusDays(40), today.minusDays(38),
                "Antananarivo Centre-ville", RentalMode.WITHOUT_DRIVER, true, false, PaymentMethod.ON_PICKUP));
    }

    private Booking demoBooking(String ref, User u, Car car, Driver driver, LocalDate s, LocalDate e,
                                String place, RentalMode mode, boolean insurance, boolean delivery,
                                PaymentMethod pm) {
        Booking b = new Booking();
        b.setReference(ref);
        b.setUser(u);
        b.setCar(car);
        b.setDriver(driver);
        b.setStartDate(s);
        b.setEndDate(e);
        b.setStartTime(LocalTime.of(8, 0));
        b.setEndTime(LocalTime.of(18, 0));
        b.setPickupLocation(place);
        b.setMode(mode);
        b.setInsurance(insurance);
        b.setDelivery(delivery);
        b.setPaymentMethod(pm);
        b.setTotalPrice(pricing.compute(b).total());
        return b;
    }
}
