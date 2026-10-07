package com.movieticket.movie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;

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
                .andExpect(jsonPath("$[?(@.title == 'Inception')].genre").value(contains("Sci-Fi")));
    }

    @Test
    void sampleMoviesAreSeededWhenDatabaseStartsEmpty() throws Exception {
        mockMvc.perform(get("/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.title == 'Pushpa 2')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.title == 'RRR')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.title == 'Kalki 2898 AD')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.title == 'Hi Nanna')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.title == 'Devara')]").isNotEmpty());
    }

    @Test
    void unknownMovieReturnsNotFound() throws Exception {
        mockMvc.perform(get("/movies/9999")).andExpect(status().isNotFound());
    }
}
