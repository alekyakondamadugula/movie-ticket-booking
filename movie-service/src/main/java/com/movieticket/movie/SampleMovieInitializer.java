package com.movieticket.movie;

import com.movieticket.movie.model.Movie;
import com.movieticket.movie.repository.MovieRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleMovieInitializer {

    @Bean
    CommandLineRunner addSampleMoviesWhenEmpty(MovieRepository repository) {
        return args -> {
            if (repository.count() != 0) {
                return;
            }
            repository.saveAll(java.util.List.of(
                    movie("Pushpa 2", "Action", "Telugu", 200, 8.2),
                    movie("RRR", "Action / Drama", "Telugu", 187, 8.0),
                    movie("Kalki 2898 AD", "Sci-Fi / Action", "Telugu", 181, 7.1),
                    movie("Hi Nanna", "Romance / Drama", "Telugu", 155, 8.3),
                    movie("Devara", "Action / Drama", "Telugu", 176, 7.0)
            ));
        };
    }

    private Movie movie(String title, String genre, String language, int duration, double rating) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setGenre(genre);
        movie.setLanguage(language);
        movie.setDuration(duration);
        movie.setRating(rating);
        return movie;
    }
}
