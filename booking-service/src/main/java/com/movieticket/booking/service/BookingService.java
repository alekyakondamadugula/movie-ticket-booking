package com.movieticket.booking.service;

import com.movieticket.booking.exception.BadRequestException;
import com.movieticket.booking.exception.ResourceNotFoundException;
import com.movieticket.booking.model.Booking;
import com.movieticket.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository repository;

    public BookingService(BookingRepository repository) {
        this.repository = repository;
    }

    public Booking create(Booking booking) {
        if (booking.getUserId() == null || booking.getMovieId() == null
                || booking.getShowTime() == null || booking.getShowTime().trim().isEmpty()) {
            throw new BadRequestException("userId, movieId and showTime are required");
        }
        if (booking.getSeats() <= 0) {
            throw new BadRequestException("seats must be greater than 0");
        }
        booking.setId(null);
        booking.setStatus("CONFIRMED");
        return repository.save(booking);
    }

    public Booking getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + id));
    }

    public List<Booking> getByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    public Booking cancel(Long id) {
        Booking booking = getById(id);
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new BadRequestException("Booking " + id + " is already cancelled");
        }
        booking.setStatus("CANCELLED");
        return repository.save(booking);
    }
}
