package com.diago.api;

import com.diago.api.Dtos.CarDto;
import com.diago.api.Dtos.CarRequest;
import com.diago.model.Car;
import com.diago.model.CarType;
import com.diago.model.Category;
import com.diago.service.AdminService;
import com.diago.service.CarForm;
import com.diago.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cars")
@Tag(name = "Voitures", description = "Catalogue (public) et gestion (ADMIN)")
public class CarApiController {

    private final CarService carService;
    private final AdminService admin;

    public CarApiController(CarService carService, AdminService admin) {
        this.carService = carService; this.admin = admin;
    }

    @GetMapping
    @Operation(summary = "Lister les voitures (filtres optionnels)")
    public List<CarDto> list(@RequestParam(required = false) Category category,
                             @RequestParam(required = false) CarType type,
                             @RequestParam(required = false) Long maxPrice) {
        return carService.search(category, type, maxPrice).stream().map(Dtos::toDto).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détails d'une voiture")
    public CarDto get(@PathVariable Long id) {
        return Dtos.toDto(find(id));
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Disponibilité d'une voiture sur une période")
    public Map<String, Object> availability(@PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Map.of("carId", id, "startDate", startDate, "endDate", endDate,
                "available", carService.isAvailable(find(id), startDate, endDate));
    }

    // ---- Réservé aux administrateurs ----
    @PostMapping
    @Operation(summary = "[ADMIN] Ajouter une voiture")
    public ResponseEntity<CarDto> create(@Valid @RequestBody CarRequest r) {
        Car c = admin.saveCar(null, toForm(r));
        return ResponseEntity.status(HttpStatus.CREATED).body(Dtos.toDto(c));
    }

    @PutMapping("/{id}")
    @Operation(summary = "[ADMIN] Modifier une voiture")
    public CarDto update(@PathVariable Long id, @Valid @RequestBody CarRequest r) {
        find(id);
        return Dtos.toDto(admin.saveCar(id, toForm(r)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "[ADMIN] Supprimer une voiture (refusé si elle a des réservations)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        find(id);
        String err = admin.deleteCar(id);
        if (err != null) throw new IllegalStateException(err);
        return ResponseEntity.noContent().build();
    }

    private Car find(Long id) {
        try {
            return carService.get(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voiture introuvable");
        }
    }

    private CarForm toForm(CarRequest r) {
        CarForm f = new CarForm();
        f.setBrand(r.brand()); f.setModel(r.model()); f.setCategory(r.category()); f.setType(r.type());
        f.setPricePerDay(r.pricePerDay()); f.setSeats(r.seats()); f.setTransmission(r.transmission());
        f.setFuel(r.fuel()); f.setAc(r.ac()); f.setLuggage(r.luggage()); f.setAvailable(r.available());
        f.setImageUrl(r.images().get(0));
        f.setExtraImages(String.join("\n", r.images().stream().skip(1).toList()));
        f.setDescription(r.description() == null ? "" : r.description());
        return f;
    }
}
