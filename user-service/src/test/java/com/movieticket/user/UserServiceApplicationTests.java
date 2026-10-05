package com.movieticket.user;

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
class UserServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerAndFetchUser() throws Exception {
        String json = "{\"name\":\"Asha\",\"email\":\"asha@example.com\",\"password\":\"secret\"}";

        mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("asha@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[?(@.email == 'asha@example.com')].name")
                                .value("Asha")
                );
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        String json = "{\"name\":\"Ravi\",\"email\":\"ravi@example.com\",\"password\":\"pw\"}";

        mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        )
                .andExpect(status().isCreated());

        mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        )
                .andExpect(status().isConflict());
    }

    @Test
    void unknownUserReturnsNotFound() throws Exception {
        mockMvc.perform(get("/users/9999"))
                .andExpect(status().isNotFound());
    }
}