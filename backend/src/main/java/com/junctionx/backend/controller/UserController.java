package com.junctionx.backend.controller;

import com.junctionx.backend.model.User;
import com.junctionx.backend.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
// Quick trial for local frontend
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    // List all
    @GetMapping
    public List<User> all() {
        return repo.findAll();
    }

    // Get by id
    @GetMapping("/{id}")
    public Optional<User> byId(@PathVariable Long id) {
        return repo.findById(id);
    }

    // Create
    @PostMapping
    public User create(@RequestBody User user) {
        // Validación sencilla de email duplicado para la demo
        if (repo.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        return repo.save(user);
    }

    // Delete for demo
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
