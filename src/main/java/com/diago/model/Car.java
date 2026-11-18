package com.diago.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private CarType type;

    private long pricePerDay;
    private int seats;

    @Enumerated(EnumType.STRING)
    private Transmission transmission;

    @Enumerated(EnumType.STRING)
    private Fuel fuel;

    private boolean ac;
    private int luggage;
    private boolean available;

    @Column(length = 500)
    private String imageUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(length = 500)
    private List<String> gallery = new ArrayList<>();

    private double rating;
    private int reviews;

    @Column(length = 500)
    private String description;

    protected Car() {}

    public Car(String brand, String model, Category category, CarType type, long pricePerDay, int seats,
               Transmission transmission, Fuel fuel, boolean ac, int luggage, boolean available,
               List<String> images, double rating, int reviews, String description) {
        this.brand = brand; this.model = model; this.category = category; this.type = type;
        this.pricePerDay = pricePerDay; this.seats = seats; this.transmission = transmission;
        this.fuel = fuel; this.ac = ac; this.luggage = luggage; this.available = available;
        this.imageUrl = images.get(0); this.gallery = new ArrayList<>(images);
        this.rating = rating; this.reviews = reviews; this.description = description;
    }

    /** Mise à jour depuis l'espace admin. */
    public void apply(String brand, String model, Category category, CarType type, long pricePerDay, int seats,
                      Transmission transmission, Fuel fuel, boolean ac, int luggage, boolean available,
                      List<String> images, String description) {
        this.brand = brand; this.model = model; this.category = category; this.type = type;
        this.pricePerDay = pricePerDay; this.seats = seats; this.transmission = transmission;
        this.fuel = fuel; this.ac = ac; this.luggage = luggage; this.available = available;
        this.imageUrl = images.get(0); this.gallery = new ArrayList<>(images);
        this.description = description;
    }

    public String getFullName() { return brand + " " + model; }

    public Long getId() { return id; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public Category getCategory() { return category; }
    public CarType getType() { return type; }
    public long getPricePerDay() { return pricePerDay; }
    public int getSeats() { return seats; }
    public Transmission getTransmission() { return transmission; }
    public Fuel getFuel() { return fuel; }
    public boolean isAc() { return ac; }
    public int getLuggage() { return luggage; }
    public boolean isAvailable() { return available; }
    public String getImageUrl() { return imageUrl; }
    public List<String> getGallery() { return gallery; }
    public double getRating() { return rating; }
    public int getReviews() { return reviews; }
    public String getDescription() { return description; }

    @Override public boolean equals(Object o) {
        return o instanceof Car c && id != null && id.equals(c.id);
    }
    @Override public int hashCode() { return Objects.hashCode(id); }

    // Options proposées pour cette voiture (null = oui, pour les voitures déjà enregistrées)
    private Boolean driverAvailable, insuranceAvailable, gpsAvailable, babySeatAvailable, deliveryAvailable, returnHomeAvailable;

    public void setOptions(boolean driver, boolean insurance, boolean gps, boolean babySeat, boolean delivery, boolean returnHome) {
        this.driverAvailable = driver; this.insuranceAvailable = insurance; this.gpsAvailable = gps;
        this.babySeatAvailable = babySeat; this.deliveryAvailable = delivery; this.returnHomeAvailable = returnHome;
    }
    public boolean isDriverAvailable() { return driverAvailable == null || driverAvailable; }
    public boolean isInsuranceAvailable() { return insuranceAvailable == null || insuranceAvailable; }
    public boolean isGpsAvailable() { return gpsAvailable == null || gpsAvailable; }
    public boolean isBabySeatAvailable() { return babySeatAvailable == null || babySeatAvailable; }
    public boolean isDeliveryAvailable() { return deliveryAvailable == null || deliveryAvailable; }
    public boolean isReturnHomeAvailable() { return returnHomeAvailable == null || returnHomeAvailable; }
}
