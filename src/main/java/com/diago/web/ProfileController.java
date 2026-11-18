package com.diago.web;

import com.diago.config.CurrentUser;
import com.diago.model.SavedAddress;
import com.diago.model.User;
import com.diago.repository.UserRepository;
import com.diago.service.BookingService;
import com.diago.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final CurrentUser current;
    private final UserService userService;
    private final UserRepository users;
    private final BookingService bookingService;

    public ProfileController(CurrentUser current, UserService userService, UserRepository users,
                             BookingService bookingService) {
        this.current = current; this.userService = userService; this.users = users;
        this.bookingService = bookingService;
    }

    /** Écran 15 : Profil */
    @GetMapping
    public String profile(Model model) {
        User u = current.get();
        model.addAttribute("bookingCount", bookingService.forUser(u).size());
        return "profile/profile";
    }

    @GetMapping("/info")
    public String info() { return "profile/info"; }

    @PostMapping("/info")
    public String saveInfo(@RequestParam String fullName, @RequestParam String phone, RedirectAttributes flash) {
        if (fullName.isBlank() || phone.isBlank()) {
            flash.addFlashAttribute("error", "Tous les champs sont obligatoires");
            return "redirect:/profile/info";
        }
        String err = userService.updateProfile(current.get().getId(), fullName, phone);
        if (err != null) {
            flash.addFlashAttribute("error", err);
        } else {
            flash.addFlashAttribute("success", "Informations mises à jour.");
        }
        return "redirect:/profile/info";
    }

    @GetMapping("/payments")
    public String payments() { return "profile/payments"; }

    @GetMapping("/addresses")
    public String addresses() { return "profile/addresses"; }

    @PostMapping("/addresses")
    @Transactional
    public String addAddress(@RequestParam String label, @RequestParam String address, RedirectAttributes flash) {
        if (label.isBlank() || address.isBlank()) {
            flash.addFlashAttribute("error", "Nom et adresse obligatoires");
        } else {
            User u = users.findById(current.get().getId()).orElseThrow();
            u.getAddresses().add(new SavedAddress(u, label.trim(), address.trim()));
            flash.addFlashAttribute("success", "Adresse ajoutée");
        }
        return "redirect:/profile/addresses";
    }

    @PostMapping("/addresses/{id}/delete")
    @Transactional
    public String deleteAddress(@PathVariable Long id) {
        User u = users.findById(current.get().getId()).orElseThrow();
        u.getAddresses().removeIf(a -> a.getId().equals(id));
        return "redirect:/profile/addresses";
    }

    @GetMapping("/settings")
    public String settings() { return "profile/settings"; }

    @GetMapping("/help")
    public String help() { return "profile/help"; }
}
