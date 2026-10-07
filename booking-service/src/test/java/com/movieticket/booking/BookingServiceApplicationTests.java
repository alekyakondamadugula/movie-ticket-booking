package com.movieticket.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
class BookingServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createFetchAndCancelBooking() throws Exception {
        String json = "{\"userId\":42,\"movieId\":1,\"showDate\":\"" + LocalDate.now()
                + "\",\"showTime\":\"5:00 PM\",\"seatNumbers\":[\"A1\",\"A2\"]}";

        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(get("/bookings/user/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seats").value(2))
                .andExpect(jsonPath("$[0].seatNumbers[0]").value("A1"));

        mockMvc.perform(put("/bookings/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void unknownBookingReturnsNotFound() throws Exception {
        mockMvc.perform(get("/bookings/9999")).andExpect(status().isNotFound());
    }

    @Test
    void availabilityReturnsConfiguredDatesAndShowtimes() throws Exception {
        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/bookings/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.showDates[0]").value(today.toString()))
                .andExpect(jsonPath("$.showDates[1]").value(today.plusDays(1).toString()))
                .andExpect(jsonPath("$.showDates[2]").value(today.plusDays(2).toString()))
                .andExpect(jsonPath("$.showTimes[0]").value("10:00 AM"))
                .andExpect(jsonPath("$.showTimes[3]").value("8:30 PM"));
    }

    @Test
    void invalidBookingReturnsBadRequest() throws Exception {
        String json = "{\"userId\":1,\"movieId\":1,\"showDate\":\"" + LocalDate.now()
                + "\",\"showTime\":\"5:00 PM\",\"seatNumbers\":[]}";
        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void seatsAreReservedPerShowAndReleasedOnCancellation() throws Exception {
        String date = LocalDate.now().toString();
        String json = "{\"userId\":42,\"movieId\":77,\"showDate\":\"" + date
                + "\",\"showTime\":\"5:00 PM\",\"seatNumbers\":[\"A1\"]}";
        String booking = mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(booking).get("id").asLong();

        mockMvc.perform(get("/bookings/availability").param("movieId", "77").param("showDate", date).param("showTime", "5:00 PM"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bookedSeats[0]").value("A1"));
        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/bookings/availability").param("movieId", "77").param("showDate", date).param("showTime", "8:30 PM"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bookedSeats").isEmpty());
        mockMvc.perform(put("/bookings/" + id + "/cancel")).andExpect(status().isOk());
        mockMvc.perform(get("/bookings/availability").param("movieId", "77").param("showDate", date).param("showTime", "5:00 PM"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bookedSeats").isEmpty());
    }
}
