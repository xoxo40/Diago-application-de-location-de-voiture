package com.diago.service;

import com.diago.model.*;
import com.diago.repository.BookingRepository;
import com.diago.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class BookingService {

    private final BookingRepository bookings;
    private final DriverRepository drivers;
    private final PricingService pricing;

    public BookingService(BookingRepository bookings, DriverRepository drivers, PricingService pricing) {
        this.bookings = bookings; this.drivers = drivers; this.pricing = pricing;
    }

    public List<Booking> forUser(User user) { return bookings.findByUserOrderByStartDateDesc(user); }

    public Optional<Booking> findOwned(Long id, User user) {
        return bookings.findById(id).filter(b -> b.getUser().getId().equals(user.getId()));
    }

    public Optional<Booking> findOwnedByReference(String ref, User user) {
        return bookings.findByReference(ref).filter(b -> b.getUser().getId().equals(user.getId()));
    }

    /** Vérifie les dates d'une réservation ; retourne un message d'erreur ou null. */
    public String validateDates(LocalDate start, LocalDate end, LocalTime startTime, LocalTime endTime) {
        if (start == null || end == null || startTime == null || endTime == null)
            return "Veuillez renseigner les dates et les heures";
        if (start.isBefore(LocalDate.now())) return "La date de départ ne peut pas être dans le passé";
        if (end.isBefore(start)) return "La date de retour doit être après la date de départ";
        return null;
    }

    /** Crée la réservation à partir du brouillon en session (site web, après paiement). */
    @Transactional
    public Booking create(User user, Car car, BookingDraft d, PaymentMethod method) {
        return create(user, car, new BookingRequest(car.getId(), d.getPickupLocation(), d.getStartDate(),
                d.getEndDate(), d.getStartTime(), d.getEndTime(), d.getMode(), d.isInsurance(), d.isGps(),
                d.isBabySeat(), d.isDelivery(), d.isReturnHome(), method));
    }

    /** Crée une réservation (utilisé par le site web ET par l'API REST). */
    @Transactional
    public Booking create(User user, Car car, BookingRequest r) {
        String err = validateDates(r.startDate(), r.endDate(), r.startTime(), r.endTime());
        if (err != null) throw new IllegalArgumentException(err);
        if (!car.isAvailable() || bookings.carBusy(car.getId(), r.startDate(), r.endDate(), -1L)) {
            throw new IllegalStateException("Cette voiture n'est plus disponible sur ces dates.");
        }
        Booking b = new Booking();
        b.setReference(nextReference());
        b.setUser(user);
        b.setCar(car);
        b.setStartDate(r.startDate());
        b.setEndDate(r.endDate());
        b.setStartTime(r.startTime());
        b.setEndTime(r.endTime());
        b.setPickupLocation(r.pickupLocation());
        b.setMode(r.mode());
        b.setInsurance(r.insurance());
        b.setGps(r.gps());
        b.setBabySeat(r.babySeat());
        b.setDelivery(r.delivery());
        b.setReturnHome(r.returnHome());
        b.setPaymentMethod(r.paymentMethod() != null ? r.paymentMethod() : PaymentMethod.ON_PICKUP);
        applyDriver(b);
        b.setTotalPrice(pricing.compute(b).total());
        return bookings.save(b);
    }

    /** Modifie une réservation à venir. Retourne un message d'erreur ou null. */
    @Transactional
    public String update(Booking b, BookingForm f) {
        String err = validateDates(f.getStartDate(), f.getEndDate(), f.getStartTime(), f.getEndTime());
        if (err != null) return err;
        if (bookings.carBusy(b.getCar().getId(), f.getStartDate(), f.getEndDate(), b.getId()))
            return "Cette voiture n'est pas disponible sur ces nouvelles dates";
        b.setPickupLocation(f.getPickupLocation());
        b.setStartDate(f.getStartDate());
        b.setEndDate(f.getEndDate());
        b.setStartTime(f.getStartTime());
        b.setEndTime(f.getEndTime());
        if (f.getMode() != null) b.setMode(f.getMode());
        b.setInsurance(f.isInsurance());
        b.setGps(f.isGps());
        b.setBabySeat(f.isBabySeat());
        b.setDelivery(f.isDelivery());
        b.setReturnHome(f.isReturnHome());
        applyDriver(b);
        b.setTotalPrice(pricing.compute(b).total());
        bookings.save(b);
        return null;
    }

    @Transactional
    public void cancel(Booking b) {
        b.setCancelled(true);

        bookings.save(b);
    }
    @Transactional
    public void confirmReceived(Booking b) {
        b.setFulfillment(FulfillmentStatus.DELIVERED);
        b.setClientConfirmedAt(LocalDateTime.now());
        bookings.save(b);
    }
    /** Assigne un chauffeur libre si la location est « avec chauffeur ». */
    private void applyDriver(Booking b) {
        if (b.getMode() != RentalMode.WITH_DRIVER) { b.setDriver(null); return; }
        Long self = b.getId() == null ? -1L : b.getId();
        if (b.getDriver() != null && !bookings.driverBusy(b.getDriver().getId(), b.getStartDate(), b.getEndDate(), self)) return;
        List<Driver> all = drivers.findAll();
        Driver chosen = all.stream()
                .filter(d -> !bookings.driverBusy(d.getId(), b.getStartDate(), b.getEndDate(), self))
                .findFirst()
                .orElse(all.isEmpty() ? null : all.get(0));
        b.setDriver(chosen);
    }

    private String nextReference() {
        long n = bookings.count() + 1000;
        String ref;
        do {
            ref = "DGO-" + LocalDate.now().getYear() + "-" + String.format("%04d", ++n);
        } while (bookings.existsByReference(ref));
        return ref;
    }
}
