package com.movieticket.booking.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long movieId;
    private String showTime;
    private int seats;
    private String status;   // CONFIRMED or CANCELLED

    public Booking() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
