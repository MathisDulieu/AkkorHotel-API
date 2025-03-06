package com.akkorhotel.hotel.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateHotelRequest {
    private String name;
    private String description;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String googleMapsUrl;
    private List<String> amenities;
    private int stars;
}
