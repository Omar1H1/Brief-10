package fr.simplon.ForkNow.controller;

import fr.simplon.ForkNow.model.Dto.RestaurantDto;
import fr.simplon.ForkNow.model.Restaurant;
import fr.simplon.ForkNow.model.User;
import fr.simplon.ForkNow.service.impl.RestaurantServiceImpl;
import fr.simplon.ForkNow.service.impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private RestaurantServiceImpl restaurantService;

    SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();


    /**
     * Retrieves the home page and adds a new User object to the model.
     *
     * @param  model the Model object to add the User object to
     * @return        the name of the view to render
     */
    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        Optional<User> maybeUser = userService.from(authentication);
        maybeUser.ifPresent(user -> model.addAttribute("userLoggedIn", user));
        Boolean isOwner = maybeUser.map(user -> {
            List<Restaurant> restaurantList = restaurantService.getAllRestaurants();
            return restaurantList.stream().anyMatch(restaurantToFind -> restaurantToFind.getOwner().equals(user));
        }).orElse(false);
        model.addAttribute("isOwner", isOwner);
        return "index";
    }

    /**
     * Retrieves the signup page and adds a new User object to the model.
     * <p>
     * The User object is used to collect user information in the signup form.
     *
     * @param  model the Model object to add the User object to
     * @return        the name of the view to render
     */
    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("userInfo", new User());
        return "signup";
    }

    /**
     * Handles POST requests to the "/signup" endpoint, creating a new User from the provided ModelAttribute and saving it to the database.
     * <p>
     * After saving the User, the user is redirected to the login page.
     *
     * @param userInfo the User object containing the provided user information
     * @param model    the Model object to add any necessary attributes to
     * @return a string representing the name of the view to render
     */
    @PostMapping("/signup")
    public String signup(@ModelAttribute("userInfo") User userInfo, Model model) {
        userService.saveUser(userInfo);
        return "redirect:/login";
    }

    /**
     * Handles GET requests to the "/login" endpoint, adding a new User object to the model and returning the "login" view.
     *
     * @param model the Model object to add the User object to
     * @return the name of the view to render
     */
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("userInfo", new User());

        return "login";
    }

    /**
     * Handles POST requests to the "/login" endpoint, checking if the provided userInfo matches any existing user.
     * If a match is found and the password matches, the user is redirected to the home page.
     * Otherwise, the user is redirected back to the login page.
     *
     * @param userInfo the User object containing the provided username and password
     * @param model the Model object to add any necessary attributes to
     * @return a string representing the name of the view to render
     */
    @PostMapping("/login")
    public String login(@ModelAttribute("userInfo") User userInfo, Model model) {
        if(userService.findByUsername(userInfo.getUsername()).isPresent()) {
            if(userService.findByUsername(userInfo.getUsername()).get().getPassword().equals(userInfo.getPassword())) {
                return "redirect:/";
            }
        }
        return "redirect:/login";
    }

    /**
     * Logs out the user by clearing the security context and redirects to the login page.
     *
     * @return a string representing the name of the view to render after the logout
     */
    @GetMapping("/logout")
    public String logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("hey 1");
        this.logoutHandler.logout(request, response, authentication);
        return "redirect:/login";
    }

    /**
     * Handles GET requests to the "/restaurants" endpoint, returning the "restaurants" view.
     *
     * @return the name of the view to render
     */
    @GetMapping("/restaurants")
    public String restaurants(Model model) {
        List<Restaurant> restaurants = restaurantService.getAllRestaurants();

        model.addAttribute("restaurants", restaurants);
        return "restaurants";
    }

    /**
     * Handles GET requests to the "/restaurants/{name}" endpoint, retrieving a specific restaurant by its name.
     *
     * @param name    the name of the restaurant to retrieve
     * @param model   the Model object to add any necessary attributes to
     * @return        the name of the view to render, either "restaurant" if found or "notfound" otherwise
     */
    @GetMapping("/restaurants/{name}")
    public String restaurant(@PathVariable String name, Model model) {
        Optional<Restaurant> maybeRestaurant = restaurantService.getRestaurantByName(name);
        if (maybeRestaurant.isPresent()) {
            model.addAttribute("restaurant", maybeRestaurant.get());
            return "restaurant";
        } else {
            return "notfound";
        }
    }

    /**
     * Handles GET requests to the "/restaurants/create" endpoint, adding a new RestaurantDto object to the model and
     * returning the "createrestaurant" view.
     *
     * @param model the Model object to add the RestaurantDto object to
     * @return the name of the view to render
     */
    @GetMapping("/restaurants/create")
    public String restaurant(Model model) {
        model.addAttribute("restaurant", new RestaurantDto());
        return "createrestaurant";
    }

    /**
     * Handles POST requests to the "/restaurants/create" endpoint, creating a new restaurant based on the provided RestaurantDto object.
     *
     * @param  restaurant  the RestaurantDto object containing the details of the restaurant to be created
     * @param  authentication  the Authentication object representing the currently logged-in user
     * @return          a string representing the redirect URL after successful creation
     */
    @PostMapping("/restaurants/create")
    public String createRestaurant(@ModelAttribute("restaurant") RestaurantDto restaurant, Authentication authentication) {

        User activeUser = userService.from(authentication).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Restaurant restaurantToSave = new Restaurant();

        String [] menuItems = restaurant.getMenu().split(",");

        for (String menuItem : menuItems) {
            restaurantToSave.addMenuItem(menuItem);
        }

        restaurantToSave.setName(restaurant.getName());
        restaurantToSave.setAddress(restaurant.getAddress());
        restaurantToSave.setCity(restaurant.getCity());
        restaurantToSave.setPicture(restaurant.getPicture());
        restaurantToSave.setOwner((activeUser));

        restaurantService.saveRestaurant(restaurantToSave);
        return "redirect:/";
    }

    @GetMapping("/ownerpanel")
    public String ownerPanel(Model model, Authentication authentication) {
        List<Restaurant> restaurantsList = restaurantService.getAllRestaurants();

        User activeUser = userService.from(authentication).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Restaurant restaurantToFind = restaurantsList.stream().filter(restaurant -> restaurant.getOwner().equals(activeUser)).findFirst().orElse(null);
        if(restaurantToFind != null) {
            model.addAttribute("restaurant", restaurantToFind);
            return "ownerpanel";
        } else {
            return "redirect:/";
        }
    }

    /**
     * Handles GET requests to the "/restaurants/delete" endpoint, deleting a restaurant if the active user is its owner.
     *
     * @param  authentication  the Authentication object representing the currently logged-in user, used to get the active user restaurant
     * @return          a string representing the redirect URL after successful deletion
     */
    @GetMapping("/restaurants/delete")
    public String deleteRestaurant(Authentication authentication) {
        User activeUser = userService.from(authentication).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<Restaurant> restaurantsList = restaurantService.getAllRestaurants();
        Restaurant restaurantToFind = restaurantsList.stream().filter(restaurant -> restaurant.getOwner().equals(activeUser)).findFirst().orElse(null);
        if(restaurantToFind != null) {
            System.out.println("Here 1");
            restaurantService.deleteRestaurant(restaurantToFind.getName());
            return "redirect:/";
        } else {
            System.out.println("Here 2");
            return "redirect:/";
        }
    }

    /**
     * Handles any GET requests to an unknown endpoint, returning a custom "404 Not Found" page.
     *
     * @return the name of the view to render
     */
    @GetMapping("/*")
    public String notFound() {
        return "notfound";
    }

    @GetMapping("/profile/{username}")
    public String profile(Model model, Authentication authentication,  @PathVariable String username) {
        User activeUser = userService.from(authentication).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        User userToFind = userService.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Restaurant restaurantToFind = restaurantService.getAllRestaurants().stream().filter(restaurant -> restaurant.getOwner().equals(userToFind)).findFirst().orElse(null);
        if(restaurantToFind != null) {
            System.out.println("Here 1");
            model.addAttribute("restaurant", restaurantToFind);
        }
        if (userToFind.equals(activeUser)) {
            System.out.println("here 2");
            model.addAttribute("user", activeUser);
            return "profile";
        }


        return "notfound";
    }

}
