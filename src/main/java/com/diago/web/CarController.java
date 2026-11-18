package com.diago.web;

import com.diago.config.CurrentUser;
import com.diago.model.Car;
import com.diago.model.CarType;
import com.diago.model.Category;
import com.diago.model.RentalMode;
import com.diago.model.User;
import com.diago.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class CarController {

    /** Une voiture + son état pour les dates choisies (affichage des cartes). */
    public record CarView(Car car, boolean available, boolean favorite) {
        public Car getCar() { return car; }
        public boolean isAvailable() { return available; }
        public boolean isFavorite() { return favorite; }
    }

    private final CarService carService;
    private final BookingService bookingService;
    private final BookingDraft draft;
    private final CurrentUser current;

    public CarController(CarService carService, BookingService bookingService,
                         BookingDraft draft, CurrentUser current) {
        this.carService = carService; this.bookingService = bookingService;
        this.draft = draft; this.current = current;
    }

    /** Écran 5 : Recherche / Réservation (formulaire) */
    @GetMapping("/search")
    public String search(Model model) {
        model.addAttribute("draft", draft);
        return "search";
    }

    /** Écran 6 : Résultats (avec filtres) */
    @GetMapping("/cars")
    public String results(@RequestParam(required = false) String pickup,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
                          @RequestParam(required = false) Category category,
                          @RequestParam(required = false) CarType type,
                          @RequestParam(required = false) Long maxPrice,
                          @RequestParam(required = false) RentalMode mode,
                          Model model, RedirectAttributes flash) {

        // La recherche (lieu + dates) est mémorisée dans le brouillon de réservation
        if (startDate != null || endDate != null) {
            LocalDate s = startDate != null ? startDate : draft.getStartDate();
            LocalDate e = endDate != null ? endDate : draft.getEndDate();
            String err = bookingService.validateDates(s, e,
                    startTime != null ? startTime : draft.getStartTime(),
                    endTime != null ? endTime : draft.getEndTime());
            if (err != null) {
                flash.addFlashAttribute("error", err);
                return "redirect:/search";
            }
            draft.setStartDate(s);
            draft.setEndDate(e);
        }
        if (pickup != null && !pickup.isBlank()) draft.setPickupLocation(pickup.trim());
        if (startTime != null) draft.setStartTime(startTime);
        if (endTime != null) draft.setEndTime(endTime);
        if (mode != null) draft.setMode(mode);

        User user = current.get();
        Set<Long> favIds = user.getFavorites().stream().map(Car::getId).collect(Collectors.toSet());
        List<CarView> cars = carService.search(category, type, maxPrice).stream()
                .map(c -> new CarView(c, carService.isAvailable(c, draft.getStartDate(), draft.getEndDate()),
                        favIds.contains(c.getId())))
                .toList();

        model.addAttribute("cars", cars);
        model.addAttribute("draft", draft);
        model.addAttribute("days", PricingService.days(draft.getStartDate(), draft.getEndDate()));
        model.addAttribute("driverPerDay", PricingService.DRIVER_PER_DAY);
        model.addAttribute("categories", Category.values());
        model.addAttribute("types", CarType.values());
        model.addAttribute("fCategory", category);
        model.addAttribute("fType", type);
        model.addAttribute("fMaxPrice", maxPrice);
        return "results";
    }

    /** Écran 7 : Détails de la voiture */
    @GetMapping("/cars/{id}")
    public String details(@PathVariable Long id, Model model) {
        Car car = carService.get(id);
        User user = current.get();
        model.addAttribute("car", car);
        model.addAttribute("favorite", user.getFavorites().contains(car));
        model.addAttribute("available", carService.isAvailable(car, draft.getStartDate(), draft.getEndDate()));
        model.addAttribute("draft", draft);
        model.addAttribute("days", PricingService.days(draft.getStartDate(), draft.getEndDate()));
        model.addAttribute("driverPerDay", PricingService.DRIVER_PER_DAY);

        model.addAttribute("insurancePerDay", PricingService.INSURANCE_PER_DAY);
        model.addAttribute("gpsPrice", PricingService.GPS);
        model.addAttribute("babyPrice", PricingService.BABY_SEAT);
        model.addAttribute("deliveryPrice", PricingService.DELIVERY);
        model.addAttribute("returnPrice", PricingService.RETURN_HOME);
        return "car-details";
    }
}
