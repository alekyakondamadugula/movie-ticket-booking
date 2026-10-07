package com.movieticket.booking.service;

import com.movieticket.booking.exception.BadRequestException;
import com.movieticket.booking.exception.ResourceNotFoundException;
import com.movieticket.booking.model.Booking;
import com.movieticket.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final List<String> SHOW_TIMES = List.of("10:00 AM", "1:30 PM", "5:00 PM", "8:30 PM");
    private static final List<String> SEAT_LAYOUT = List.of("A1", "A2", "A3", "A4", "A5", "A6",
            "B1", "B2", "B3", "B4", "B5", "B6", "C1", "C2", "C3", "C4", "C5", "C6",
            "D1", "D2", "D3", "D4", "D5", "D6");

    private final BookingRepository repository;

    public BookingService(BookingRepository repository) {
        this.repository = repository;
    }

    public synchronized Booking create(Booking booking) {
        if (booking.getUserId() == null || booking.getMovieId() == null
                || booking.getShowDate() == null || booking.getShowTime() == null) {
            throw new BadRequestException("userId, movieId, showDate and showTime are required");
        }
        if (!getShowDates().contains(booking.getShowDate()) || !SHOW_TIMES.contains(booking.getShowTime())) {
            throw new BadRequestException("Select a configured show date and showtime");
        }
        List<String> seats = booking.getSeatNumbers().stream().distinct().toList();
        if (seats.isEmpty() || seats.size() != booking.getSeatNumbers().size() || !SEAT_LAYOUT.containsAll(seats))
            throw new BadRequestException("Select one or more valid seats");
        Set<String> bookedSeats = getBookedSeats(booking.getMovieId(), booking.getShowDate(), booking.getShowTime());
        if (seats.stream().anyMatch(bookedSeats::contains))
            throw new BadRequestException("One or more selected seats have just been booked. Refresh seat availability.");
        booking.setSeatNumbers(seats);
        booking.setId(null);
        booking.setStatus("CONFIRMED");
        return repository.save(booking);
    }

    public List<String> getShowDates() {
        LocalDate today = LocalDate.now();
        return List.of(today.toString(), today.plusDays(1).toString(), today.plusDays(2).toString());
    }

    public Map<String, Object> getAvailability(Long movieId, String showDate, String showTime) {
        if (movieId == null) return availabilityResponse(Set.of());
        if (showDate == null || showTime == null || !getShowDates().contains(showDate) || !SHOW_TIMES.contains(showTime))
            throw new BadRequestException("Select a configured movie, date and showtime");
        Set<String> booked = getBookedSeats(movieId, showDate, showTime);
        return availabilityResponse(booked);
    }

    private Map<String, Object> availabilityResponse(Set<String> booked) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("showDates", getShowDates());
        result.put("showTimes", SHOW_TIMES);
        result.put("seats", SEAT_LAYOUT);
        result.put("bookedSeats", booked);
        return result;
    }

    private Set<String> getBookedSeats(Long movieId, String showDate, String showTime) {
        return repository.findConfirmedSeats(movieId, showDate, showTime).stream()
                .flatMap(booking -> booking.getSeatNumbers().stream()).collect(Collectors.toSet());
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
