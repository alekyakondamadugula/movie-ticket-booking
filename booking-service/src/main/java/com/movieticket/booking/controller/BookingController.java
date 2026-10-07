package com.movieticket.booking.controller;

import com.movieticket.booking.model.Booking;
import com.movieticket.booking.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking create(@RequestBody Booking booking) {
        return service.create(booking);
    }

    @GetMapping("/{id}")
    public Booking getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Booking> getByUser(@PathVariable Long userId) {
        return service.getByUser(userId);
    }

    @GetMapping("/availability")
    public Map<String, Object> getAvailability(@RequestParam(required = false) Long movieId,
                                               @RequestParam(required = false) String showDate,
                                               @RequestParam(required = false) String showTime) {
        return service.getAvailability(movieId, showDate, showTime);
    }

    @PutMapping("/{id}/cancel")
    public Booking cancel(@PathVariable Long id) {
        return service.cancel(id);
    }
}
