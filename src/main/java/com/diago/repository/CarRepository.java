package com.diago.repository;

import com.diago.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findAllByOrderByPricePerDayAsc();
    List<Car> findTop4ByAvailableTrueOrderByRatingDescReviewsDesc();
}
