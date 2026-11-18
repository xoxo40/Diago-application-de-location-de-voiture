package com.diago.web;

import com.diago.config.CurrentUser;
import com.diago.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FavoriteController {

    private final UserService userService;
    private final CurrentUser current;

    public FavoriteController(UserService userService, CurrentUser current) {
        this.userService = userService; this.current = current;
    }

    /** Écran 14 : Favoris */
    @GetMapping("/favorites")
    public String favorites(Model model) {
        model.addAttribute("cars", current.get().getFavorites());
        return "favorites";
    }

    @PostMapping("/favorites/{carId}/toggle")
    public String toggle(@PathVariable Long carId, @RequestParam(defaultValue = "/favorites") String redirect,
                         RedirectAttributes flash) {
        boolean added = userService.toggleFavorite(current.get().getId(), carId);
        flash.addFlashAttribute("success", added ? "Ajouté à vos favoris ♥" : "Retiré de vos favoris");
        // Sécurité : on n'accepte que des redirections internes
        if (!redirect.startsWith("/") || redirect.startsWith("//")) redirect = "/favorites";
        return "redirect:" + redirect;
    }
}
