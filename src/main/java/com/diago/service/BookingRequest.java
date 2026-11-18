package com.diago.service;

import com.diago.model.PaymentMethod;
import com.diago.model.RentalMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/** Données d'une réservation (corps JSON de l'API, ou brouillon du site web). */
public record BookingRequest(
        Long carId,
        @NotBlank(message = "Le lieu de prise en charge est obligatoire") String pickupLocation,
        @NotNull(message = "startDate obligatoire (AAAA-MM-JJ)") LocalDate startDate,
        @NotNull(message = "endDate obligatoire (AAAA-MM-JJ)") LocalDate endDate,
        @NotNull(message = "startTime obligatoire (HH:mm)") LocalTime startTime,
        @NotNull(message = "endTime obligatoire (HH:mm)") LocalTime endTime,
        @NotNull(message = "mode obligatoire : WITH_DRIVER ou WITHOUT_DRIVER") RentalMode mode,
        boolean insurance, boolean gps, boolean babySeat, boolean delivery, boolean returnHome,
        PaymentMethod paymentMethod) {
}
