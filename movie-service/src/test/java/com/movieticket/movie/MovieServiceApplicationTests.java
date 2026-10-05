package com.movieticket.movie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MovieServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addAndFetchMovie() throws Exception {
        String json = "{\"title\":\"Inception\",\"genre\":\"Sci-Fi\",\"language\":\"English\","
                + "\"duration\":148,\"rating\":8.8}";
        mockMvc.perform(post("/movies").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Inception"));

        mockMvc.perform(get("/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].genre").value("Sci-Fi"));
    }

    @Test
    void unknownMovieReturnsNotFound() throws Exception {
        mockMvc.perform(get("/movies/9999")).andExpect(status().isNotFound());
    }
}
