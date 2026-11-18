package com.diago.repository;

import com.diago.model.Booking;
import com.diago.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserOrderByStartDateDesc(User user);

    boolean existsByReference(String reference);

    boolean existsByCarId(Long carId);

    boolean existsByDriverId(Long driverId);

    List<Booking> findAllByOrderByCreatedAtDesc();
    List<Booking> findByDriverIdAndCancelledFalseOrderByStartDateAsc(Long driverId);

    java.util.Optional<Booking> findByReference(String reference);

    /** Vrai si la voiture est déjà réservée (non annulée) sur la période [start, end]. */
    @Query("""
            select count(b) > 0 from Booking b
            where b.car.id = :carId and b.cancelled = false
              and b.startDate <= :end and b.endDate >= :start
              and b.id <> :excludeId
            """)
    boolean carBusy(@Param("carId") Long carId, @Param("start") LocalDate start,
                    @Param("end") LocalDate end, @Param("excludeId") Long excludeId);

    @Query("""
            select count(b) > 0 from Booking b
            where b.driver.id = :driverId and b.cancelled = false
              and b.startDate <= :end and b.endDate >= :start
              and b.id <> :excludeId
            """)
    boolean driverBusy(@Param("driverId") Long driverId, @Param("start") LocalDate start,
                       @Param("end") LocalDate end, @Param("excludeId") Long excludeId);
}
