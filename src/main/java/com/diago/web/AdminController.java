package com.diago.web;

import com.diago.model.*;
import com.diago.service.AdminService;
import com.diago.service.CarForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.diago.service.FileStorageService;

/** Espace administrateur (accessible uniquement au rôle ADMIN, voir SecurityConfig). */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService admin;
    private final FileStorageService storage;

    public AdminController(AdminService admin, FileStorageService storage) {
        this.admin = admin;
        this.storage = storage;
    }
    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }
    /** Appelée toutes les 15 s par la page pour mettre à jour la pastille. */
    @GetMapping("/pending-count")
    @ResponseBody
    public long pendingCount() { return admin.stats().pending(); }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", admin.stats());
        model.addAttribute("latest", admin.latestBookings(6));
        model.addAttribute("pendingList", admin.pendingBookings());
        model.addAttribute("tab", "dashboard");
        return "admin/dashboard";
    }

    // ---------- Voitures ----------
    @GetMapping("/cars")
    public String cars(Model model) {
        model.addAttribute("cars", admin.allCars());
        model.addAttribute("tab", "cars");
        return "admin/cars";
    }

    @GetMapping("/cars/new")
    public String newCar(Model model) {
        model.addAttribute("form", new CarForm());
        addCarFormData(model, null);
        return "admin/car-form";
    }

    @GetMapping("/cars/{id}/edit")
    public String editCar(@PathVariable Long id, Model model) {
        // Le formulaire est rempli avec les données actuelles de la voiture
        model.addAttribute("form", CarForm.from(admin.getCar(id)));
        addCarFormData(model, id);
        return "admin/car-form";
    }

    @PostMapping({"/cars", "/cars/{id}"})
    public String saveCar(@PathVariable(required = false) Long id, @Valid @ModelAttribute("form") CarForm form,
                          BindingResult result, Model model, RedirectAttributes flash) {
        // Photos choisies depuis l'ordinateur
        try {
            String main = storage.save(form.getPhoto());
            if (main != null) form.setImageUrl(main);
            String extras = String.join("\n", storage.saveAll(form.getExtraPhotos()));
            if (!extras.isEmpty()) {
                String old = form.getExtraImages() == null ? "" : form.getExtraImages().trim();
                form.setExtraImages(old.isEmpty() ? extras : old + "\n" + extras);
            }
        } catch (IllegalArgumentException e) {
            result.rejectValue("imageUrl", "image", e.getMessage());
        }
        if (form.getImageUrl() == null || form.getImageUrl().isBlank()) {
            result.rejectValue("imageUrl", "required", "Ajoutez une photo principale.");
        }
        if (result.hasErrors()) {
            addCarFormData(model, id);
            return "admin/car-form";
        }
        admin.saveCar(id, form);
        flash.addFlashAttribute("success", id == null ? "Voiture ajoutée." : "Voiture modifiée.");
        return "redirect:/admin/cars";
    }

    @PostMapping("/cars/{id}/toggle")
    public String toggleCar(@PathVariable Long id) {
        admin.toggleCarAvailability(id);
        return "redirect:/admin/cars";
    }

    @PostMapping("/cars/{id}/delete")
    public String deleteCar(@PathVariable Long id, RedirectAttributes flash) {
        String err = admin.deleteCar(id);
        flash.addFlashAttribute(err == null ? "success" : "error", err == null ? "Voiture supprimée." : err);
        return "redirect:/admin/cars";
    }

    private void addCarFormData(Model model, Long id) {
        model.addAttribute("carId", id);
        model.addAttribute("categories", Category.values());
        model.addAttribute("types", CarType.values());
        model.addAttribute("transmissions", Transmission.values());
        model.addAttribute("fuels", Fuel.values());
        model.addAttribute("tab", "cars");
    }

    // ---------- Chauffeurs ----------
    @GetMapping("/drivers")
    public String drivers(Model model) {
        model.addAttribute("drivers", admin.allDrivers());
        model.addAttribute("tab", "drivers");
        return "admin/drivers";
    }

    @PostMapping("/drivers")
    public String saveDriver(@RequestParam(required = false) Long id, @RequestParam String name,
                             @RequestParam double rating, @RequestParam String phone,
                             @RequestParam String photoUrl, RedirectAttributes flash) {
        if (name.isBlank() || phone.isBlank() || rating < 0 || rating > 5) {
            flash.addFlashAttribute("error", "Nom et téléphone obligatoires, note entre 0 et 5.");
        } else {
            admin.saveDriver(id, name, rating, phone, photoUrl);
            flash.addFlashAttribute("success", "Chauffeur enregistré.");
        }
        return "redirect:/admin/drivers";
    }

    @PostMapping("/drivers/{id}/delete")
    public String deleteDriver(@PathVariable Long id, RedirectAttributes flash) {
        String err = admin.deleteDriver(id);
        flash.addFlashAttribute(err == null ? "success" : "error", err == null ? "Chauffeur supprimé." : err);
        return "redirect:/admin/drivers";
    }

    // ---------- Réservations ----------
    @GetMapping("/bookings")
    public String bookings(Model model) {
        model.addAttribute("bookings", admin.allBookings());
        model.addAttribute("tab", "bookings");
        return "admin/bookings";
    }

    @PostMapping("/bookings/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam FulfillmentStatus to, RedirectAttributes flash) {
        admin.setFulfillment(id, to);
        flash.addFlashAttribute("success", "Réservation mise à jour : " + to.getLabel() + ".");
        return "redirect:/admin/bookings";
    }

    // ---------- Clients ----------
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", admin.allUsers());
        model.addAttribute("tab", "users");
        return "admin/users";
    }
}
