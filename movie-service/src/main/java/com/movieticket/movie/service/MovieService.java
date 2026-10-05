package com.movieticket.movie.service;

import com.movieticket.movie.exception.BadRequestException;
import com.movieticket.movie.exception.ResourceNotFoundException;
import com.movieticket.movie.model.Movie;
import com.movieticket.movie.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepository repository;

    public MovieService(MovieRepository repository) {
        this.repository = repository;
    }

    public Movie add(Movie movie) {
        if (movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
            throw new BadRequestException("title is required");
        }
        movie.setId(null);
        return repository.save(movie);
    }

    public List<Movie> getAll() {
        return repository.findAll();
    }

    public Movie getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id " + id));
    }
}
