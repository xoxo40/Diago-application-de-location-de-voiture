package com.diago.service;

import com.diago.model.*;
import com.diago.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Opérations de l'espace administrateur. */
@Service
public class AdminService {

    private final CarRepository cars;
    private final DriverRepository drivers;
    private final UserRepository users;
    private final BookingRepository bookings;

    public AdminService(CarRepository cars, DriverRepository drivers, UserRepository users, BookingRepository bookings) {
        this.cars = cars; this.drivers = drivers; this.users = users; this.bookings = bookings;
    }

    public record Stats(long clients, long cars, long drivers, long bookings, long activeBookings, long pending, long revenue) {
        public long getClients() { return clients; }
        public long getCars() { return cars; }
        public long getDrivers() { return drivers; }
        public long getBookings() { return bookings; }
        public long getActiveBookings() { return activeBookings; }
        public long getPending() { return pending; }
        public long getRevenue() { return revenue; }
    }

    public Stats stats() {
        List<Booking> all = bookings.findAll();
        long active = all.stream().filter(b -> b.getStatus() == BookingStatus.ACTIVE).count();
        long pending = all.stream().filter(b -> !b.isCancelled() && b.getFulfillment() == FulfillmentStatus.PENDING).count();
        long revenue = all.stream().filter(b -> !b.isCancelled()).mapToLong(Booking::getTotalPrice).sum();
        long clients = users.findAll().stream().filter(u -> !u.isAdmin()).count();
        return new Stats(clients, cars.count(), drivers.count(), all.size(), active, pending, revenue);
    }

    public List<Booking> latestBookings(int n) {
        return bookings.findAllByOrderByCreatedAtDesc().stream().limit(n).toList();
    }

    public List<Booking> pendingBookings() {
        return bookings.findAllByOrderByCreatedAtDesc().stream()
                .filter(b -> !b.isCancelled() && b.getFulfillment() == FulfillmentStatus.PENDING).toList();
    }

    public List<Booking> allBookings() { return bookings.findAllByOrderByCreatedAtDesc(); }
    public List<User> allUsers() { return users.findAll(); }
    public Car getCar(Long id) { return cars.findById(id).orElseThrow(); }
    public List<Car> allCars() { return cars.findAllByOrderByPricePerDayAsc(); }
    public List<Driver> allDrivers() { return drivers.findAll(); }

    // ---- Voitures ----
    @Transactional
    public Car saveCar(Long id, CarForm f) {
        Car c;
        if (id == null) {
            c = new Car(f.getBrand().trim(), f.getModel().trim(), f.getCategory(), f.getType(), f.getPricePerDay(),
                    f.getSeats(), f.getTransmission(), f.getFuel(), f.isAc(), f.getLuggage(), f.isAvailable(),
                    f.images(), 4.5, 0, f.getDescription());
        } else {
            c = cars.findById(id).orElseThrow();
            c.apply(f.getBrand().trim(), f.getModel().trim(), f.getCategory(), f.getType(), f.getPricePerDay(),
                    f.getSeats(), f.getTransmission(), f.getFuel(), f.isAc(), f.getLuggage(), f.isAvailable(),
                    f.images(), f.getDescription());
        }
        c.setOptions(f.isDriverAvailable(), f.isInsuranceAvailable(), f.isGpsAvailable(),
                f.isBabySeatAvailable(), f.isDeliveryAvailable(), f.isReturnHomeAvailable());
        return cars.save(c);
    }

    @Transactional
    public String deleteCar(Long id) {
        Car car = cars.findById(id).orElseThrow();
        if (bookings.existsByCarId(id)) {
            return "Impossible : cette voiture a des réservations. Marquez-la plutôt comme indisponible.";
        }
        for (User u : users.findAll()) u.getFavorites().remove(car);
        cars.delete(car);
        return null;
    }

    @Transactional
    public void toggleCarAvailability(Long id) {
        Car c = cars.findById(id).orElseThrow();
        CarForm f = CarForm.from(c);
        f.setAvailable(!c.isAvailable());
        saveCar(id, f);
    }

    // ---- Chauffeurs ----
    @Transactional
    public void saveDriver(Long id, String name, double rating, String phone, String photoUrl) {
        if (id == null) drivers.save(new Driver(name.trim(), rating, phone.trim(), photoUrl.trim()));
        else drivers.findById(id).orElseThrow().apply(name.trim(), rating, phone.trim(), photoUrl.trim());
    }

    @Transactional
    public String deleteDriver(Long id) {
        if (bookings.existsByDriverId(id)) return "Impossible : ce chauffeur est lié à des réservations.";
        drivers.deleteById(id);
        return null;
    }

    // ---- Réservations ----
    /** Change l'étape de suivi d'une réservation (valider, livrer, refuser...). */
    @Transactional
    public void setFulfillment(Long id, FulfillmentStatus status) {
        Booking b = bookings.findById(id).orElseThrow();
        b.setFulfillment(status);
        b.setCancelled(status == FulfillmentStatus.REJECTED);
        // Si l'admin revient en arrière, la confirmation du client n'est plus valable
        if (status != FulfillmentStatus.DELIVERED && status != FulfillmentStatus.RETURNED) {
            b.setClientConfirmedAt(null);
        }
        // La livraison démarre : on attribue un chauffeur libre s'il n'y en a pas encore
        if (status == FulfillmentStatus.IN_DELIVERY && b.getDriver() == null) {
            var all = drivers.findAll();
            b.setDriver(all.stream()
                    .filter(d -> !bookings.driverBusy(d.getId(), b.getStartDate(), b.getEndDate(), b.getId()))
                    .findFirst()
                    .orElse(all.isEmpty() ? null : all.get(0)));
        }
    }
}