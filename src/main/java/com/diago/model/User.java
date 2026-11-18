package com.diago.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToMany
    @JoinTable(name = "user_favorites",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "car_id"))
    private Set<Car> favorites = new LinkedHashSet<>();

    /** "USER" (client) ou "ADMIN" (espace administrateur). */
    private String role = "USER";

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavedAddress> addresses = new ArrayList<>();

    protected User() {}

    public User(String fullName, String phone, String email, String password) {
        this.fullName = fullName; this.phone = phone; this.email = email; this.password = password;
    }

    public String getFirstName() {
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    public String getInitials() {
        StringBuilder sb = new StringBuilder();
        for (String p : fullName.trim().split("\\s+")) {
            if (!p.isEmpty() && sb.length() < 2) sb.append(Character.toUpperCase(p.charAt(0)));
        }
        return sb.toString();
    }

    public String getRole() { return role == null ? "USER" : role; }
    public void setRole(String role) { this.role = role; }
    public boolean isAdmin() { return "ADMIN".equals(getRole()); }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Set<Car> getFavorites() { return favorites; }
    public List<SavedAddress> getAddresses() { return addresses; }
}
