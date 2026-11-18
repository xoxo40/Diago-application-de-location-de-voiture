package com.diago.api;

import com.diago.model.*;
import com.diago.service.PriceBreakdown;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Objets échangés en JSON entre le client et le serveur (on n'expose jamais les entités JPA directement). */
public final class Dtos {

    private Dtos() {}

    public record CarDto(Long id, String brand, String model, String category, String type, long pricePerDay,
                         int seats, String transmission, String fuel, boolean ac, int luggage, boolean available,
                         String imageUrl, List<String> gallery, double rating, int reviews, String description) {}

    public record DriverDto(String name, double rating, String phone, String photoUrl) {}

    public record UserDto(Long id, String fullName, String email, String phone, String role) {}

    public record BookingDto(Long id, String reference, Long carId, String carName, String status,
                             LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime,
                             String pickupLocation, String mode, boolean insurance, boolean gps, boolean babySeat,
                             boolean delivery, boolean returnHome, String paymentMethod, long totalPrice,
                             boolean modifiable, DriverDto driver, PriceBreakdown price) {}

    public record RegisterRequest(
            @NotBlank(message = "Le nom complet est obligatoire") String fullName,
            @NotBlank @Pattern(regexp = "^[+0-9 ]{9,18}$", message = "Numéro invalide") String phone,
            @NotBlank @Email(message = "Email invalide") String email,
            @NotBlank @Size(min = 6, message = "6 caractères minimum") String password) {}

    public record CarRequest(
            @NotBlank String brand, @NotBlank String model,
            @NotNull Category category, @NotNull CarType type,
            @Min(1000) long pricePerDay, @Min(1) @Max(60) int seats,
            @NotNull Transmission transmission, @NotNull Fuel fuel,
            boolean ac, @Min(0) int luggage, boolean available,
            @NotEmpty(message = "Au moins une image (URL)") List<String> images,
            @Size(max = 500) String description) {}

    // ---- Conversions entité -> DTO ----
    public static CarDto toDto(Car c) {
        return new CarDto(c.getId(), c.getBrand(), c.getModel(), c.getCategory().name(), c.getType().name(),
                c.getPricePerDay(), c.getSeats(), c.getTransmission().name(), c.getFuel().name(), c.isAc(),
                c.getLuggage(), c.isAvailable(), c.getImageUrl(), List.copyOf(c.getGallery()), c.getRating(),
                c.getReviews(), c.getDescription());
    }

    public static DriverDto toDto(Driver d) {
        return d == null ? null : new DriverDto(d.getName(), d.getRating(), d.getPhone(), d.getPhotoUrl());
    }

    public static UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getRole());
    }

    public static BookingDto toDto(Booking b, PriceBreakdown price) {
        return new BookingDto(b.getId(), b.getReference(), b.getCar().getId(), b.getCar().getFullName(),
                b.getStatus().name(), b.getStartDate(), b.getEndDate(), b.getStartTime(), b.getEndTime(),
                b.getPickupLocation(), b.getMode().name(), b.isInsurance(), b.isGps(), b.isBabySeat(),
                b.isDelivery(), b.isReturnHome(), b.getPaymentMethod().name(), b.getTotalPrice(),
                b.isModifiable(), toDto(b.getDriver()), price);
    }
}
