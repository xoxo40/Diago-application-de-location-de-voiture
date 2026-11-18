package com.diago.web;

import com.diago.config.CurrentUser;
import com.diago.model.Booking;
import com.diago.model.BookingStatus;
import com.diago.model.User;
import com.diago.service.BookingForm;
import com.diago.service.BookingService;
import com.diago.service.PricingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/bookings")
public class MyBookingsController {

    private final BookingService bookingService;
    private final PricingService pricing;
    private final CurrentUser current;

    public MyBookingsController(BookingService bookingService, PricingService pricing, CurrentUser current) {
        this.bookingService = bookingService; this.pricing = pricing; this.current = current;
    }

    /** Écran 12 : Mes réservations (à venir / en cours / terminées) */
    @GetMapping
    public String list(Model model) {
        List<Booking> all = bookingService.forUser(current.get());
        model.addAttribute("upcoming", all.stream().filter(b -> b.getStatus() == BookingStatus.UPCOMING).toList());
        model.addAttribute("ongoing", all.stream().filter(b -> b.getStatus() == BookingStatus.ACTIVE).toList());
        model.addAttribute("completed", all.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).toList());
        model.addAttribute("cancelled", all.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).toList());
        model.addAttribute("total", all.size());
        return "bookings";
    }

    /** Écran 13 : Détails de la réservation */
    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        User user = current.get();
        return bookingService.findOwned(id, user).map(b -> {
            model.addAttribute("booking", b);
            model.addAttribute("price", pricing.compute(b));
            return "booking-details";
        }).orElse("redirect:/bookings");
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes flash) {
        bookingService.findOwned(id, current.get()).ifPresent(b -> {
            if (b.isModifiable()) {
                bookingService.cancel(b);
                flash.addFlashAttribute("success", "Réservation " + b.getReference() + " annulée.");
            } else {
                flash.addFlashAttribute("error", "Seule une réservation à venir peut être annulée.");
            }
        });
        return "redirect:/bookings/" + id;
    }
    @PostMapping("/{id}/received")
    public String received(@PathVariable Long id, RedirectAttributes flash) {
        bookingService.findOwned(id, current.get()).ifPresent(b -> {
            if (b.isReceivable()) {
                bookingService.confirmReceived(b);
                flash.addFlashAttribute("success", "Merci ! Nous avons bien noté que votre voiture est arrivée.");
            } else {
                flash.addFlashAttribute("error", "Cette réservation n'est pas en cours de livraison.");
            }
        });
        return "redirect:/bookings/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes flash) {
        return bookingService.findOwned(id, current.get()).map(b -> {
            if (!b.isModifiable()) {
                flash.addFlashAttribute("error", "Seule une réservation à venir peut être modifiée.");
                return "redirect:/bookings/" + id;
            }
            model.addAttribute("booking", b);
            model.addAttribute("price", pricing.compute(b));
            return "booking-edit";
        }).orElse("redirect:/bookings");
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @ModelAttribute BookingForm form, RedirectAttributes flash) {
        return bookingService.findOwned(id, current.get()).map(b -> {
            if (!b.isModifiable()) {
                flash.addFlashAttribute("error", "Seule une réservation à venir peut être modifiée.");
                return "redirect:/bookings/" + id;
            }
            String err = bookingService.update(b, form);
            if (err != null) {
                flash.addFlashAttribute("error", err);
                return "redirect:/bookings/" + id + "/edit";
            }
            flash.addFlashAttribute("success", "Réservation modifiée.");
            return "redirect:/bookings/" + id;
        }).orElse("redirect:/bookings");
    }
    /** Position du chauffeur, lue en boucle par la carte du client. */
    @GetMapping("/{id}/driver-location")
    @ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> driverLocation(@PathVariable Long id) {
        return bookingService.findOwned(id, current.get()).map(b -> {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            var d = b.getDriver();
            boolean live = d != null
                    && b.getFulfillment() == com.diago.model.FulfillmentStatus.IN_DELIVERY
                    && d.getLat() != null && d.getLng() != null;
            m.put("fulfillment", b.getFulfillment().name());
            m.put("notice", b.getDriverNotice());
            m.put("hasPosition", live);
            if (live) {
                m.put("lat", d.getLat());
                m.put("lng", d.getLng());
            }
            return org.springframework.http.ResponseEntity.ok(m);
        }).orElse(org.springframework.http.ResponseEntity.notFound().build());
    }

}
