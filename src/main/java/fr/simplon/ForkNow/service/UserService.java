package fr.simplon.ForkNow.service;

import fr.simplon.ForkNow.model.User;

import java.util.Optional;

public interface UserService {
    void saveUser(User userMapping);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
