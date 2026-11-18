package com.diago.web;

import com.diago.config.CurrentUser;
import com.diago.model.*;
import com.diago.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Tunnel de réservation : options -> récapitulatif -> paiement -> confirmation. */
@Controller
@RequestMapping("/booking")
public class BookingFlowController {

    private final CarService carService;
    private final BookingService bookingService;
    private final PricingService pricing;
    private final BookingDraft draft;
    private final CurrentUser current;

    public BookingFlowController(CarService carService, BookingService bookingService, PricingService pricing,
                                 BookingDraft draft, CurrentUser current) {
        this.carService = carService; this.bookingService = bookingService; this.pricing = pricing;
        this.draft = draft; this.current = current;
    }

    /** Depuis les détails de la voiture : choix du mode puis « Continuer ». */
    @PostMapping("/start")
    public String start(@RequestParam Long carId, @ModelAttribute BookingForm form, RedirectAttributes flash) {
        Car car = carService.get(carId);
        if (!carService.isAvailable(car, draft.getStartDate(), draft.getEndDate())) {
            flash.addFlashAttribute("error", "Cette voiture n'est pas disponible sur ces dates.");
            return "redirect:/cars/" + carId;
        }
        draft.setCarId(carId);
        draft.setMode(form.getMode() != null ? form.getMode() : RentalMode.WITHOUT_DRIVER);
        draft.setInsurance(form.isInsurance());
        draft.setGps(form.isGps());
        draft.setBabySeat(form.isBabySeat());
        draft.setDelivery(form.isDelivery());
        draft.setReturnHome(form.isReturnHome());
        return "redirect:/booking/summary";
    }

    /** Écran 8 : Options de location */
    @GetMapping("/options")
    public String options(Model model) {
        if (draft.getCarId() == null) return "redirect:/search";
        Car car = carService.get(draft.getCarId());
        model.addAttribute("car", car);
        model.addAttribute("draft", draft);
        model.addAttribute("days", PricingService.days(draft.getStartDate(), draft.getEndDate()));
        model.addAttribute("price", priceOf(car));
        model.addAttribute("driverPerDay", PricingService.DRIVER_PER_DAY);
        model.addAttribute("insurancePerDay", PricingService.INSURANCE_PER_DAY);
        model.addAttribute("gpsPrice", PricingService.GPS);
        model.addAttribute("babyPrice", PricingService.BABY_SEAT);
        model.addAttribute("deliveryPrice", PricingService.DELIVERY);
        model.addAttribute("returnPrice", PricingService.RETURN_HOME);
        return "options";
    }

    @PostMapping("/options")
    public String saveOptions(@ModelAttribute BookingForm form) {
        if (draft.getCarId() == null) return "redirect:/search";
        if (form.getMode() != null) draft.setMode(form.getMode());
        draft.setInsurance(form.isInsurance());
        draft.setGps(form.isGps());
        draft.setBabySeat(form.isBabySeat());
        draft.setDelivery(form.isDelivery());
        draft.setReturnHome(form.isReturnHome());
        return "redirect:/booking/summary";
    }

    /** Écran 9 : Récapitulatif */
    @GetMapping("/summary")
    public String summary(Model model) {
        if (draft.getCarId() == null) return "redirect:/search";
        Car car = carService.get(draft.getCarId());
        model.addAttribute("car", car);
        model.addAttribute("draft", draft);
        model.addAttribute("price", priceOf(car));
        return "summary";
    }

    /** Écran 10 : Paiement */
    @GetMapping("/payment")
    public String payment(Model model) {
        if (draft.getCarId() == null) return "redirect:/search";
        Car car = carService.get(draft.getCarId());
        model.addAttribute("car", car);
        model.addAttribute("draft", draft);
        model.addAttribute("price", priceOf(car));
        model.addAttribute("methods", PaymentMethod.values());
        return "payment";
    }

    @PostMapping("/pay")
    public String pay(@RequestParam PaymentMethod method, RedirectAttributes flash) {
        if (draft.getCarId() == null) return "redirect:/search";
        Car car = carService.get(draft.getCarId());
        try {
            Booking b = bookingService.create(current.get(), car, draft, method);
            draft.clearBooking();
            return "redirect:/booking/confirmation/" + b.getReference();
        } catch (IllegalStateException | IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/cars/" + car.getId();
        }
    }

    /** Écran 11 : Confirmation */
    @GetMapping("/confirmation/{reference}")
    public String confirmation(@PathVariable String reference, Model model) {
        return bookingService.findOwnedByReference(reference, current.get())
                .map(b -> { model.addAttribute("booking", b); return "confirmation"; })
                .orElse("redirect:/bookings");
    }

    private PriceBreakdown priceOf(Car car) {
        return pricing.compute(car, draft.getStartDate(), draft.getEndDate(), draft.getMode(),
                draft.isInsurance(), draft.isGps(), draft.isBabySeat(), draft.isDelivery(), draft.isReturnHome());
    }
}
