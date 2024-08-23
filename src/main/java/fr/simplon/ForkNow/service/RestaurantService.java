package fr.simplon.ForkNow.service;

import fr.simplon.ForkNow.model.Restaurant;
import fr.simplon.ForkNow.model.User;

import java.util.List;
import java.util.Optional;

public interface RestaurantService {
    void saveRestaurant(Restaurant restaurant);
    Optional<Restaurant> getRestaurantById(Long id);
    List<Restaurant> getAllRestaurants();

    Optional<Restaurant> getRestaurantByName(String name);

    void deleteRestaurant(String restaurantName);

}
