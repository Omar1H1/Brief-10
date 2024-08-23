package fr.simplon.ForkNow.repository;

import fr.simplon.ForkNow.model.Restaurant;
import fr.simplon.ForkNow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findByName(String name);
    void deleteByName(String name);
}
