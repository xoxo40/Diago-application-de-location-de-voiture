package com.diago.service;

import com.diago.model.Booking;
import com.diago.model.Driver;
import com.diago.model.FulfillmentStatus;
import com.diago.model.User;
import com.diago.repository.BookingRepository;
import com.diago.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

/** Ce que peut faire un chauffeur dans son espace. */
@Service
public class DriverService {

    private final DriverRepository drivers;
    private final BookingRepository bookings;

    public DriverService(DriverRepository drivers, BookingRepository bookings) {
        this.drivers = drivers; this.bookings = bookings;
    }

    public Optional<Driver> forAccount(User account) {
        return account == null ? Optional.empty() : drivers.findByAccountId(account.getId());
    }

    public List<Booking> missions(Driver d) {
        return bookings.findByDriverIdAndCancelledFalseOrderByStartDateAsc(d.getId());
    }

    @Transactional
    public void updateLocation(Driver d, double lat, double lng) {
        d.updateLocation(lat, lng);
        drivers.save(d);
    }

    /** Le chauffeur fait avancer sa livraison : départ, puis remise de la voiture. */
    @Transactional
    public boolean advance(Long bookingId, Driver d, FulfillmentStatus to) {
        Booking b = bookings.findById(bookingId).orElse(null);
        if (b == null || b.isCancelled() || b.getDriver() == null
                || !b.getDriver().getId().equals(d.getId())) return false;
        FulfillmentStatus now = b.getFulfillment();
        boolean ok = (to == FulfillmentStatus.IN_DELIVERY && now == FulfillmentStatus.CONFIRMED && b.isDelivery())
                || (to == FulfillmentStatus.DELIVERED && now == FulfillmentStatus.IN_DELIVERY);
        if (!ok) return false;
        b.setFulfillment(to);
        if (to == FulfillmentStatus.IN_DELIVERY) { b.setDriverNotice(null); b.setDriverNoticeAt(null); }
        bookings.save(b);
        return true;
    }
    /** Le chauffeur prévient le client : bientôt arrivé (NEAR) ou arrivé (ARRIVED). */
    @Transactional
    public boolean notice(Long bookingId, Driver d, String type) {
        Booking b = bookings.findById(bookingId).orElse(null);
        if (b == null || b.isCancelled() || b.getDriver() == null
                || !b.getDriver().getId().equals(d.getId())) return false;
        if (b.getFulfillment() != FulfillmentStatus.IN_DELIVERY) return false;
        if (!"NEAR".equals(type) && !"ARRIVED".equals(type)) return false;
        b.setDriverNotice(type);
        b.setDriverNoticeAt(LocalDateTime.now());
        bookings.save(b);
        return true;
    }
}