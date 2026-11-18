package com.diago.web;

import com.diago.model.Category;
import com.diago.service.BookingDraft;
import com.diago.service.CarService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final CarService carService;
    private final BookingDraft draft;

    public HomeController(CarService carService, BookingDraft draft) {
        this.carService = carService; this.draft = draft;
    }

    /** Écran 4 : Accueil (dashboard) */
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("categories", Category.values());
        model.addAttribute("popular", carService.popular());
        model.addAttribute("draft", draft);
        return "home";
    }
}
