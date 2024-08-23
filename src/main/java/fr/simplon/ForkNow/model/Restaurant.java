package fr.simplon.ForkNow.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Entity
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String address;

    private String city;

    private String picture;

    @OneToOne
    private User Owner;

    @ElementCollection
    private List<String> menu;

    public Restaurant(String name, String address, String city, User owner, String picture, List<String> menu) {
        this.name = name;
        this.address = address;
        this.city = city;
        Owner = owner;
        this.picture = picture;
        this.menu = new ArrayList<>();
    }

    public void addMenuItem(String menuItem) {
        menu.add(menuItem);
    }

    public Restaurant() {
        this.menu = new ArrayList<>();
    }
}
