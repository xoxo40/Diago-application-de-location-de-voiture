package com.diago.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double rating;
    private String phone;

    @Column(length = 500)
    private String photoUrl;

    /** Compte de connexion du chauffeur (espace chauffeur). */
    @OneToOne
    private User account;

    /** Dernière position GPS partagée par le chauffeur. */
    private Double lat;
    private Double lng;
    private LocalDateTime locationUpdatedAt;

    protected Driver() {}

    public Driver(String name, double rating, String phone, String photoUrl) {
        this.name = name; this.rating = rating; this.phone = phone; this.photoUrl = photoUrl;
    }

    public void apply(String name, double rating, String phone, String photoUrl) {
        this.name = name; this.rating = rating; this.phone = phone; this.photoUrl = photoUrl;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public double getRating() { return rating; }
    public String getPhone() { return phone; }
    public String getPhotoUrl() { return photoUrl; }


    public User getAccount() { return account; }
    public void setAccount(User account) { this.account = account; }
    public Double getLat() { return lat; }
    public Double getLng() { return lng; }
    public LocalDateTime getLocationUpdatedAt() { return locationUpdatedAt; }

    public void updateLocation(double lat, double lng) {
        this.lat = lat; this.lng = lng; this.locationUpdatedAt = LocalDateTime.now();
    }
}
