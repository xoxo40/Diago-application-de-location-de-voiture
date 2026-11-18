package com.diago.web;

import com.diago.service.AdminService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Ajoute le nombre de réservations à valider à toutes les pages admin. */
@ControllerAdvice(assignableTypes = AdminController.class)
public class AdminAdvice {

    private final AdminService admin;

    public AdminAdvice(AdminService admin) { this.admin = admin; }

    @ModelAttribute("pendingCount")
    public long pendingCount() { return admin.stats().pending(); }
}