package com.diago.service;

import com.diago.model.RentalMode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/** Formulaire (options de location et modification d'une réservation). */
public class BookingForm {

    private RentalMode mode;
    private String pickupLocation;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime startTime;
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime endTime;
    private boolean insurance, gps, babySeat, delivery, returnHome;

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
}
