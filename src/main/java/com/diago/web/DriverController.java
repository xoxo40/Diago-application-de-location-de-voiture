package com.diago.web;

import com.diago.config.CurrentUser;
import com.diago.model.Booking;
import com.diago.model.Driver;
import com.diago.model.FulfillmentStatus;
import com.diago.service.DriverService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Espace chauffeur : mes livraisons, départ, remise, partage de position. */
@Controller
@RequestMapping("/driver")
public class DriverController {

    private final DriverService service;
    private final CurrentUser current;

    public DriverController(DriverService service, CurrentUser current) {
        this.service = service; this.current = current;
    }

    @GetMapping("/login")
    public String login() { return "driver/login"; }

    @GetMapping
    public String home(Model model) {
        Driver d = service.forAccount(current.get()).orElse(null);
        if (d == null) return "redirect:/driver/login";
        List<Booking> all = service.missions(d);
        model.addAttribute("driver", d);
        model.addAttribute("todo", all.stream().filter(b ->
                b.getFulfillment() == FulfillmentStatus.CONFIRMED
                        || b.getFulfillment() == FulfillmentStatus.IN_DELIVERY
                        || b.getFulfillment() == FulfillmentStatus.DELIVERED).toList());
        model.addAttribute("done", all.stream().filter(b ->
                b.getFulfillment() == FulfillmentStatus.RETURNED).toList());
        return "driver/home";
    }

    @PostMapping("/bookings/{id}/depart")
    public String depart(@PathVariable Long id, RedirectAttributes flash) {
        Driver d = service.forAccount(current.get()).orElse(null);
        if (d != null && service.advance(id, d, FulfillmentStatus.IN_DELIVERY)) {
            flash.addFlashAttribute("success", "Livraison démarrée. Partagez votre position pour que le client vous suive.");
        } else {
            flash.addFlashAttribute("error", "Impossible de démarrer cette livraison.");
        }
        return "redirect:/driver";
    }

    @PostMapping("/bookings/{id}/delivered")
    public String delivered(@PathVariable Long id, RedirectAttributes flash) {
        Driver d = service.forAccount(current.get()).orElse(null);
        if (d != null && service.advance(id, d, FulfillmentStatus.DELIVERED)) {
            flash.addFlashAttribute("success", "Voiture remise au client. Merci !");
        } else {
            flash.addFlashAttribute("error", "Impossible de marquer cette livraison comme remise.");
        }
        return "redirect:/driver";
    }

    /** Reçoit la position GPS envoyée par le téléphone du chauffeur. */
    @PostMapping("/location")
    @ResponseBody
    public ResponseEntity<Void> location(@RequestParam double lat, @RequestParam double lng) {
        Driver d = service.forAccount(current.get()).orElse(null);
        if (d == null) return ResponseEntity.status(403).build();
        service.updateLocation(d, lat, lng);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bookings/{id}/notice")
    public String notice(@PathVariable Long id, @RequestParam String type, RedirectAttributes flash) {
        Driver d = service.forAccount(current.get()).orElse(null);
        if (d != null && service.notice(id, d, type)) {
            flash.addFlashAttribute("success", "Le client a été prévenu.");
        } else {
            flash.addFlashAttribute("error", "Impossible d'envoyer ce message.");
        }
        return "redirect:/driver";
    }

}