package com.diago.web;

import com.diago.config.CurrentUser;
import com.diago.service.RegisterForm;
import com.diago.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.diago.model.Category;
import com.diago.service.CarService;
import java.util.List;
import com.diago.model.Car;
import com.diago.model.Category;
import com.diago.repository.DriverRepository;
import com.diago.service.CarService;
import com.diago.service.PricingService;
import java.util.ArrayList;
import java.util.Comparator;

@Controller
public class AuthController {

    private final UserService userService;
    private final CurrentUser current;
    private final CarService carService;
    private final DriverRepository drivers;

    public AuthController(UserService userService, CurrentUser current,
                          CarService carService, DriverRepository drivers) {
        this.userService = userService; this.current = current;
        this.carService = carService; this.drivers = drivers;
    }

    /** Une catégorie affichée sur le site vitrine. */
    public record CategoryCard(Category category, long fromPrice, String image, int count) {
        public Category getCategory() { return category; }
        public long getFromPrice() { return fromPrice; }
        public String getImage() { return image; }
        public int getCount() { return count; }
    }

    /** Page d'accueil publique (site vitrine) */
    @GetMapping("/")
    public String splash(Model model) {
        if (current.get() != null) return "redirect:/home";

        List<Car> all = carService.search(null, null, null);

        // Diaporama : jusqu'à 5 voitures disponibles, les mieux notées d'abord
        List<Car> slides = all.stream().filter(Car::isAvailable)
                .sorted(Comparator.comparingDouble(Car::getRating).reversed())
                .limit(5).toList();

        // Cartes de catégories (photo de la mieux notée + prix de départ)
        List<CategoryCard> catalog = new ArrayList<>();
        for (Category c : Category.values()) {
            List<Car> inCat = all.stream().filter(x -> x.getCategory() == c).toList();
            if (inCat.isEmpty()) continue;
            long from = inCat.stream().mapToLong(Car::getPricePerDay).min().orElse(0);
            Car best = inCat.stream().max(Comparator.comparingDouble(Car::getRating)).get();
            catalog.add(new CategoryCard(c, from, best.getImageUrl(), inCat.size()));
        }

        model.addAttribute("slides", slides);
        model.addAttribute("catalog", catalog);
        model.addAttribute("popular", carService.popular());
        model.addAttribute("drivers", drivers.findAll());
        model.addAttribute("driverPerDay", PricingService.DRIVER_PER_DAY);
        model.addAttribute("insurancePerDay", PricingService.INSURANCE_PER_DAY);
        model.addAttribute("gpsPrice", PricingService.GPS);
        model.addAttribute("babyPrice", PricingService.BABY_SEAT);
        model.addAttribute("deliveryPrice", PricingService.DELIVERY);
        model.addAttribute("returnPrice", PricingService.RETURN_HOME);
        return "splash";
    }



    /** Écran 2 : Connexion */
    @GetMapping("/login")
    public String login() {
        return current.get() != null ? "redirect:/home" : "login";
    }

    /** Écran 3 : Inscription */
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult result,
                           HttpServletRequest request, HttpServletResponse response) {
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Les mots de passe ne correspondent pas");
        }
        if (result.hasErrors()) return "register";

        List<String> errors = userService.register(form);
        if (!errors.isEmpty()) {
            errors.forEach(e -> result.reject("duplicate", e));
            return "register";
        }
        // Connexion automatique après l'inscription -> Accueil
        UserDetails details = userService.loadUserByUsername(form.getEmail());
        Authentication auth = UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        new HttpSessionSecurityContextRepository().saveContext(ctx, request, response);
        return "redirect:/home";
    }

    @GetMapping("/forgot-password")
    public String forgot() { return "forgot-password"; }
}
