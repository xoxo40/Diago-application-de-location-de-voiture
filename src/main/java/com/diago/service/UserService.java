package com.diago.service;

import com.diago.model.Car;
import com.diago.model.User;
import com.diago.repository.CarRepository;
import com.diago.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository users;
    private final CarRepository cars;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, CarRepository cars, PasswordEncoder encoder) {
        this.users = users; this.cars = cars; this.encoder = encoder;
    }

    /** Connexion par email OU par numéro de téléphone. */
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        String id = identifier.trim();
        User u = users.findByEmailIgnoreCase(id)
                .or(() -> users.findByPhone(normalizePhone(id)))
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur inconnu"));
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail()).password(u.getPassword()).roles(u.getRole()).build();
    }

    public User findByEmail(String email) {
        return users.findByEmailIgnoreCase(email.trim()).orElseThrow();
    }

    public static String normalizePhone(String phone) {
        return phone == null ? null : phone.replaceAll("\\s+", "");
    }

    /** Valide l'unicité et crée le compte. Retourne la liste des erreurs (vide si OK). */
    @Transactional
    public List<String> register(RegisterForm f) {
        if (users.existsByEmailIgnoreCase(f.getEmail().trim())) {
            return List.of("Un compte existe déjà avec cet email");
        }
        if (users.existsByPhone(normalizePhone(f.getPhone()))) {
            return List.of("Un compte existe déjà avec ce numéro");
        }
        users.save(new User(f.getFullName().trim(), normalizePhone(f.getPhone()),
                f.getEmail().trim().toLowerCase(), encoder.encode(f.getPassword())));
        return List.of();
    }

    @Transactional
    public boolean toggleFavorite(Long userId, Long carId) {
        User u = users.findById(userId).orElseThrow();
        Car car = cars.findById(carId).orElseThrow();
        if (u.getFavorites().remove(car)) return false;
        u.getFavorites().add(car);
        return true;
    }

    /** Met à jour le profil. Retourne un message d'erreur ou null. */
    @Transactional
    public String updateProfile(Long userId, String fullName, String phone) {
        User u = users.findById(userId).orElseThrow();
        String p = normalizePhone(phone);
        if (users.findByPhone(p).filter(o -> !o.getId().equals(userId)).isPresent())
            return "Ce numéro est déjà utilisé";
        u.setFullName(fullName.trim());
        u.setPhone(p);
        return null;
    }
}
