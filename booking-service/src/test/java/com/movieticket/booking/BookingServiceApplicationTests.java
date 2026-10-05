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

@SpringBootTest
@AutoConfigureMockMvc
class BookingServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createFetchAndCancelBooking() throws Exception {
        String json = "{\"userId\":42,\"movieId\":1,\"showTime\":\"2026-10-10T18:30\",\"seats\":3}";

        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(get("/bookings/user/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seats").value(3));

        mockMvc.perform(put("/bookings/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void unknownBookingReturnsNotFound() throws Exception {
        mockMvc.perform(get("/bookings/9999")).andExpect(status().isNotFound());
    }

    @Test
    void invalidBookingReturnsBadRequest() throws Exception {
        String json = "{\"userId\":1,\"movieId\":1,\"showTime\":\"2026-10-10T18:30\",\"seats\":0}";
        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }
}
