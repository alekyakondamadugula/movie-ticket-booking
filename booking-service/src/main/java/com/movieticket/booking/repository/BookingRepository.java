package com.movieticket.booking.repository;

import com.movieticket.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    @Query("select b from Booking b join b.seatNumbers seat where b.movieId = :movieId and b.showDate = :showDate and b.showTime = :showTime and b.status = 'CONFIRMED'")
    List<Booking> findConfirmedSeats(@Param("movieId") Long movieId, @Param("showDate") String showDate,
                                    @Param("showTime") String showTime);
}
