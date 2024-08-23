package fr.simplon.ForkNow.model.Dto;

import fr.simplon.ForkNow.model.User;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDto {

    private String name;

    private String address;

    private String city;


    private User Owner;

    private String picture;

    private String menu;

}
