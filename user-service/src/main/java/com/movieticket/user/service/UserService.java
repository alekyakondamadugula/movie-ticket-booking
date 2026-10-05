package com.movieticket.user.service;

import com.movieticket.user.exception.BadRequestException;
import com.movieticket.user.exception.DuplicateEmailException;
import com.movieticket.user.exception.ResourceNotFoundException;
import com.movieticket.user.model.User;
import com.movieticket.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User register(User user) {
        if (isBlank(user.getName()) || isBlank(user.getEmail()) || isBlank(user.getPassword())) {
            throw new BadRequestException("name, email and password are required");
        }
        if (repository.existsByEmail(user.getEmail())) {
            throw new DuplicateEmailException("Email already registered: " + user.getEmail());
        }
        user.setId(null);
        return repository.save(user);
    }

    public List<User> getAll() {
        return repository.findAll();
    }

    public User getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
