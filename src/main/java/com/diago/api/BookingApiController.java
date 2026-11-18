package com.diago.api;

import com.diago.api.Dtos.BookingDto;
import com.diago.config.CurrentUser;
import com.diago.model.Booking;
import com.diago.model.Car;
import com.diago.model.PaymentMethod;
import com.diago.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Réservations", description = "Réservations de l'utilisateur connecté (authentification Basic)")
public class BookingApiController {

    private final BookingService bookingService;
    private final CarService carService;
    private final PricingService pricing;
    private final CurrentUser current;

    public BookingApiController(BookingService bookingService, CarService carService,
                                PricingService pricing, CurrentUser current) {
        this.bookingService = bookingService; this.carService = carService;
        this.pricing = pricing; this.current = current;
    }

    @GetMapping
    @Operation(summary = "Mes réservations")
    public List<BookingDto> mine() {
        return bookingService.forUser(current.get()).stream().map(this::dto).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détails d'une de mes réservations")
    public BookingDto get(@PathVariable Long id) {
        return dto(owned(id));
    }

    @PostMapping("/quote")
    @Operation(summary = "Calculer le prix sans réserver")
    public PriceBreakdown quote(@Valid @RequestBody BookingRequest r) {
        Car car = car(r.carId());
        String err = bookingService.validateDates(r.startDate(), r.endDate(), r.startTime(), r.endTime());
        if (err != null) throw new IllegalArgumentException(err);
        return pricing.compute(car, r.startDate(), r.endDate(), r.mode(),
                r.insurance(), r.gps(), r.babySeat(), r.delivery(), r.returnHome());
    }

    @PostMapping
    @Operation(summary = "Créer une réservation (paiement simulé)")
    public ResponseEntity<BookingDto> create(@Valid @RequestBody BookingRequest r) {
        Car car = car(r.carId());
        Booking b = bookingService.create(current.get(), car, r);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto(b));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une réservation à venir")
    public BookingDto update(@PathVariable Long id, @Valid @RequestBody BookingRequest r) {
        Booking b = owned(id);
        if (!b.isModifiable()) throw new IllegalStateException("Seule une réservation à venir peut être modifiée.");
        BookingForm f = new BookingForm();
        f.setMode(r.mode());
        f.setPickupLocation(r.pickupLocation());
        f.setStartDate(r.startDate());
        f.setEndDate(r.endDate());
        f.setStartTime(r.startTime());
        f.setEndTime(r.endTime());
        f.setInsurance(r.insurance());
        f.setGps(r.gps());
        f.setBabySeat(r.babySeat());
        f.setDelivery(r.delivery());
        f.setReturnHome(r.returnHome());
        String err = bookingService.update(b, f);
        if (err != null) throw new IllegalStateException(err);
        return dto(owned(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Annuler une réservation à venir")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        Booking b = owned(id);
        if (!b.isModifiable()) throw new IllegalStateException("Seule une réservation à venir peut être annulée.");
        bookingService.cancel(b);
        return ResponseEntity.noContent().build();
    }

    private Booking owned(Long id) {
        return bookingService.findOwned(id, current.get())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation introuvable"));
    }

    private Car car(Long id) {
        if (id == null) throw new IllegalArgumentException("carId obligatoire");
        try {
            return carService.get(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voiture introuvable");
        }
    }

    private BookingDto dto(Booking b) {
        return Dtos.toDto(b, pricing.compute(b));
    }
}
