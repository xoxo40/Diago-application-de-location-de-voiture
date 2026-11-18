package com.diago.config;

import com.diago.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Variables disponibles dans tous les templates. */
@ControllerAdvice
public class GlobalAdvice {

    private final CurrentUser current;

    public GlobalAdvice(CurrentUser current) { this.current = current; }

    @ModelAttribute("currentUser")
    public User currentUser() { return current.get(); }

    /** URL courante (utile pour revenir à la même page après un clic sur ♥). */
    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        String q = request.getQueryString();
        return request.getRequestURI() + (q == null ? "" : "?" + q);
    }
}
