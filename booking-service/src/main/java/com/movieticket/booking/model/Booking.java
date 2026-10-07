package com.movieticket.booking.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long movieId;
    private String movieTitle;
    private String showDate;
    private String showTime;
    private String status;   // CONFIRMED or CANCELLED

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "booking_seats", joinColumns = @JoinColumn(name = "booking_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"booking_id", "seat_number"}))
    @Column(name = "seat_number", nullable = false)
    private List<String> seatNumbers = new ArrayList<>();

    public Booking() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public String getShowDate() { return showDate; }
    public void setShowDate(String showDate) { this.showDate = showDate; }
    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }
    public List<String> getSeatNumbers() { return seatNumbers; }
    public void setSeatNumbers(List<String> seatNumbers) { this.seatNumbers = seatNumbers == null ? new ArrayList<>() : new ArrayList<>(seatNumbers); }
    public int getSeats() { return seatNumbers.size(); }
    public void setSeats(int seats) { /* retained for old clients; seat numbers are required for new bookings */ }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
