package com.movieticket;

import com.intuit.karate.junit5.Karate;

class ApiTest {
    @Karate.Test
    Karate api() {
        return Karate.run("classpath:features");
    }
}
