package com.diago.service;

import com.diago.model.PaymentMethod;
import com.diago.model.RentalMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Réservation en cours de construction (stockée en session) :
 * recherche -> voiture -> options -> récapitulatif -> paiement.
 */
@Component
@SessionScope
public class BookingDraft implements Serializable {

    private Long carId;
    private RentalMode mode = RentalMode.WITHOUT_DRIVER;
    private String pickupLocation = "Antananarivo, Ivandry";
    private LocalDate startDate = LocalDate.now().plusDays(1);
    private LocalDate endDate = LocalDate.now().plusDays(4);
    private LocalTime startTime = LocalTime.of(8, 0);
    private LocalTime endTime = LocalTime.of(18, 0);
    private boolean insurance, gps, babySeat, delivery, returnHome;
    private PaymentMethod paymentMethod = PaymentMethod.MOBILE_MONEY;

    public void resetOptions() {
        insurance = gps = babySeat = delivery = returnHome = false;
    }

    public void clearBooking() {
        carId = null;
        mode = RentalMode.WITHOUT_DRIVER;
        resetOptions();
        paymentMethod = PaymentMethod.MOBILE_MONEY;
    }

    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }
    public RentalMode getMode() { return mode; }
    public void setMode(RentalMode mode) { this.mode = mode; }
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
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
}
