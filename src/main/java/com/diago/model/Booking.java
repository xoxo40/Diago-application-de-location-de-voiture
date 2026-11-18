package com.diago.model;

import com.diago.service.PricingService;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String reference;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Car car;

    @ManyToOne
    private Driver driver;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String pickupLocation;

    @Enumerated(EnumType.STRING)
    private RentalMode mode;

    private boolean insurance;
    private boolean gps;
    private boolean babySeat;
    private boolean delivery;
    private boolean returnHome;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private long totalPrice;
    private boolean cancelled;

    @Enumerated(EnumType.STRING)
    private FulfillmentStatus fulfillment = FulfillmentStatus.PENDING;
    private Instant createdAt = Instant.now();
    private LocalDateTime clientConfirmedAt;

    /** Signalement du chauffeur : null, "NEAR" (bientôt arrivé) ou "ARRIVED" (arrivé). */
    private String driverNotice;
    private LocalDateTime driverNoticeAt;

    public Booking() {}

    /** Le statut est déduit des dates (sauf annulation). */
    public BookingStatus getStatus() {
        if (cancelled) return BookingStatus.CANCELLED;
        LocalDate today = LocalDate.now();
        if (today.isBefore(startDate)) return BookingStatus.UPCOMING;
        if (today.isAfter(endDate)) return BookingStatus.COMPLETED;
        return BookingStatus.ACTIVE;
    }

    public boolean isModifiable() { return getStatus() == BookingStatus.UPCOMING; }
    public int getDays() { return PricingService.days(startDate, endDate); }

    public Long getId() { return id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Car getCar() { return car; }
    public void setCar(Car car) { this.car = car; }
    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public RentalMode getMode() { return mode; }
    public void setMode(RentalMode mode) { this.mode = mode; }
    public boolean isInsurance() { return insurance; }
    public void setInsurance(boolean insurance) { this.insurance = insurance; }
    public boolean isGps() { return gps; }
    public void setGps(boolean gps) { this.gps = gps; }
    public boolean isBabySeat() { return babySeat; }
    public void setBabySeat(boolean babySeat) { this.babySeat = babySeat; }
    public boolean isDelivery() { return delivery; }
    public void setDelivery(boolean delivery) { this.delivery = delivery; }
    public boolean isReturnHome() { return returnHome; }
    public void setReturnHome(boolean returnHome) { this.returnHome = returnHome; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public long getTotalPrice() { return totalPrice; }
    public void setTotalPrice(long totalPrice) { this.totalPrice = totalPrice; }
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    public Instant getCreatedAt() { return createdAt; }
    public FulfillmentStatus getFulfillment() { return fulfillment == null ? FulfillmentStatus.PENDING : fulfillment; }
    public void setFulfillment(FulfillmentStatus fulfillment) { this.fulfillment = fulfillment; }

    public LocalDateTime getClientConfirmedAt() { return clientConfirmedAt; }
    public void setClientConfirmedAt(LocalDateTime clientConfirmedAt) { this.clientConfirmedAt = clientConfirmedAt; }

    public String getDriverNotice() { return driverNotice; }
    public void setDriverNotice(String driverNotice) { this.driverNotice = driverNotice; }
    public LocalDateTime getDriverNoticeAt() { return driverNoticeAt; }
    public void setDriverNoticeAt(LocalDateTime driverNoticeAt) { this.driverNoticeAt = driverNoticeAt; }

    /** Étape de suivi montrée au client : 0 = refusée, de 1 à 5 sinon. */
    public int getFulfillmentStep() {
        switch (getFulfillment()) {
            case PENDING: return 1;
            case CONFIRMED: return 2;
            case IN_DELIVERY: return 3;
            case DELIVERED: return 4;
            case RETURNED: return 5;
            default: return 0;
        }
    }

    /** Le client peut confirmer la réception tant que la voiture est en cours de livraison. */
    public boolean isReceivable() {
        return !cancelled && getFulfillment() == FulfillmentStatus.IN_DELIVERY;
    }
}