package com.diago.service;

import com.diago.model.Car;
import com.diago.model.CarType;
import com.diago.model.Category;
import com.diago.repository.BookingRepository;
import com.diago.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CarService {

    private final CarRepository cars;
    private final BookingRepository bookings;

    public CarService(CarRepository cars, BookingRepository bookings) {
        this.cars = cars; this.bookings = bookings;
    }

    /** Une voiture est disponible si elle est en service ET libre sur la période. */
    public boolean isAvailable(Car car, LocalDate start, LocalDate end) {
        return car.isAvailable() && !bookings.carBusy(car.getId(), start, end, -1L);
    }

    public List<Car> search(Category category, CarType type, Long maxPrice) {
        return cars.findAllByOrderByPricePerDayAsc().stream()
                .filter(c -> category == null || c.getCategory() == category)
                .filter(c -> type == null || c.getType() == type)
                .filter(c -> maxPrice == null || c.getPricePerDay() <= maxPrice)
                .toList();
    }

    public List<Car> popular() { return cars.findTop4ByAvailableTrueOrderByRatingDescReviewsDesc(); }

    public Car get(Long id) { return cars.findById(id).orElseThrow(() -> new IllegalArgumentException("Voiture introuvable")); }
}
