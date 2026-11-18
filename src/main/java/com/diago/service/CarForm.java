package com.diago.service;

import com.diago.model.*;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/** Formulaire admin : ajout / modification d'une voiture. */
public class CarForm {

    @NotBlank(message = "La marque est obligatoire") private String brand;
    @NotBlank(message = "Le modèle est obligatoire") private String model;
    @NotNull(message = "Choisissez une catégorie") private Category category;
    @NotNull(message = "Choisissez un type") private CarType type;
    @Min(value = 1000, message = "Prix invalide") private long pricePerDay;
    @Min(value = 1, message = "Au moins 1 place") @Max(value = 60, message = "Trop de places") private int seats = 5;
    @NotNull(message = "Choisissez une transmission") private Transmission transmission;
    @NotNull(message = "Choisissez un carburant") private Fuel fuel;
    private boolean ac = true;
    @Min(value = 0, message = "Valeur invalide") private int luggage = 2;
    private boolean available = true;
    private String imageUrl = "";
    /** Photos supplémentaires, une URL par ligne. */
    private String extraImages = "";
    @Size(max = 500, message = "500 caractères maximum") private String description = "";

    public static CarForm from(Car c) {
        CarForm f = new CarForm();
        f.brand = c.getBrand(); f.model = c.getModel(); f.category = c.getCategory(); f.type = c.getType();
        f.pricePerDay = c.getPricePerDay(); f.seats = c.getSeats(); f.transmission = c.getTransmission();
        f.fuel = c.getFuel(); f.ac = c.isAc(); f.luggage = c.getLuggage(); f.available = c.isAvailable();
        f.imageUrl = c.getImageUrl();
        f.extraImages = String.join("\n", c.getGallery().stream().skip(1).toList());
        f.description = c.getDescription();

        f.driverAvailable = c.isDriverAvailable(); f.insuranceAvailable = c.isInsuranceAvailable();
        f.gpsAvailable = c.isGpsAvailable(); f.babySeatAvailable = c.isBabySeatAvailable();
        f.deliveryAvailable = c.isDeliveryAvailable(); f.returnHomeAvailable = c.isReturnHomeAvailable();
        return f;
    }

    public List<String> images() {
        List<String> all = new ArrayList<>();
        all.add(imageUrl.trim());
        if (extraImages != null) {
            for (String line : extraImages.split("\\R")) {
                if (!line.isBlank()) all.add(line.trim());
            }
        }
        return all;
    }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public CarType getType() { return type; }
    public void setType(CarType type) { this.type = type; }
    public long getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(long pricePerDay) { this.pricePerDay = pricePerDay; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public Transmission getTransmission() { return transmission; }
    public void setTransmission(Transmission transmission) { this.transmission = transmission; }
    public Fuel getFuel() { return fuel; }
    public void setFuel(Fuel fuel) { this.fuel = fuel; }
    public boolean isAc() { return ac; }
    public void setAc(boolean ac) { this.ac = ac; }
    public int getLuggage() { return luggage; }
    public void setLuggage(int luggage) { this.luggage = luggage; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getExtraImages() { return extraImages; }
    public void setExtraImages(String extraImages) { this.extraImages = extraImages; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    private MultipartFile photo;
    private List<MultipartFile> extraPhotos = new ArrayList<>();
    public MultipartFile getPhoto() { return photo; }
    public void setPhoto(MultipartFile photo) { this.photo = photo; }
    public List<MultipartFile> getExtraPhotos() { return extraPhotos; }
    public void setExtraPhotos(List<MultipartFile> extraPhotos) { this.extraPhotos = extraPhotos; }

    private boolean driverAvailable = true, insuranceAvailable = true, gpsAvailable = true,
            babySeatAvailable = true, deliveryAvailable = true, returnHomeAvailable = true;
    public boolean isDriverAvailable() { return driverAvailable; }
    public void setDriverAvailable(boolean v) { this.driverAvailable = v; }
    public boolean isInsuranceAvailable() { return insuranceAvailable; }
    public void setInsuranceAvailable(boolean v) { this.insuranceAvailable = v; }
    public boolean isGpsAvailable() { return gpsAvailable; }
    public void setGpsAvailable(boolean v) { this.gpsAvailable = v; }
    public boolean isBabySeatAvailable() { return babySeatAvailable; }
    public void setBabySeatAvailable(boolean v) { this.babySeatAvailable = v; }
    public boolean isDeliveryAvailable() { return deliveryAvailable; }
    public void setDeliveryAvailable(boolean v) { this.deliveryAvailable = v; }
    public boolean isReturnHomeAvailable() { return returnHomeAvailable; }
    public void setReturnHomeAvailable(boolean v) { this.returnHomeAvailable = v; }
}
