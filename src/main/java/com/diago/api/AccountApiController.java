package com.diago.api;

import com.diago.api.Dtos.CarDto;
import com.diago.api.Dtos.RegisterRequest;
import com.diago.api.Dtos.UserDto;
import com.diago.config.CurrentUser;
import com.diago.model.Car;
import com.diago.model.User;
import com.diago.service.CarService;
import com.diago.service.RegisterForm;
import com.diago.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Compte", description = "Inscription, profil et favoris")
public class AccountApiController {

    private final UserService userService;
    private final CarService carService;
    private final CurrentUser current;

    public AccountApiController(UserService userService, CarService carService, CurrentUser current) {
        this.userService = userService; this.carService = carService; this.current = current;
    }

    @PostMapping("/auth/register")
    @Operation(summary = "Créer un compte (public)")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest r) {
        RegisterForm f = new RegisterForm();
        f.setFullName(r.fullName());
        f.setPhone(r.phone());
        f.setEmail(r.email());
        f.setPassword(r.password());
        List<String> errors = userService.register(f);
        if (!errors.isEmpty()) throw new IllegalStateException(errors.get(0));
        User u = userService.findByEmail(r.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(Dtos.toDto(u));
    }

    @GetMapping("/me")
    @Operation(summary = "Mon profil (sert aussi à tester ses identifiants)")
    public UserDto me() {
        return Dtos.toDto(current.get());
    }

    @GetMapping("/favorites")
    @Operation(summary = "Mes voitures favorites")
    public List<CarDto> favorites() {
        return current.get().getFavorites().stream().map(Dtos::toDto).toList();
    }

    @PutMapping("/favorites/{carId}")
    @Operation(summary = "Ajouter une voiture aux favoris")
    public ResponseEntity<Void> add(@PathVariable Long carId) {
        User u = current.get();
        if (!u.getFavorites().contains(find(carId))) userService.toggleFavorite(u.getId(), carId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/favorites/{carId}")
    @Operation(summary = "Retirer une voiture des favoris")
    public ResponseEntity<Void> remove(@PathVariable Long carId) {
        User u = current.get();
        if (u.getFavorites().contains(find(carId))) userService.toggleFavorite(u.getId(), carId);
        return ResponseEntity.noContent().build();
    }

    private Car find(Long id) {
        try {
            return carService.get(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voiture introuvable");
        }
    }
}
