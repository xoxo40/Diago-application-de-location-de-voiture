package com.diago.config;

import com.diago.model.Driver;
import com.diago.model.User;
import com.diago.repository.DriverRepository;
import com.diago.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Crée un compte de connexion pour chaque chauffeur qui n'en a pas encore. */
@Component
@Order(2)
public class DriverAccountSeeder implements CommandLineRunner {

    private final DriverRepository drivers;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public DriverAccountSeeder(DriverRepository drivers, UserRepository users, PasswordEncoder encoder) {
        this.drivers = drivers; this.users = users; this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (Driver d : drivers.findAll()) {
            if (d.getAccount() != null) continue;
            String email = "chauffeur" + d.getId() + "@diago.mg";
            User account = users.findByEmailIgnoreCase(email).orElseGet(() -> {
                User u = new User(d.getName(), d.getPhone(), email, encoder.encode("chauffeur1234"));
                u.setRole("DRIVER");
                return users.save(u);
            });
            d.setAccount(account);
            drivers.save(d);
        }
    }
}