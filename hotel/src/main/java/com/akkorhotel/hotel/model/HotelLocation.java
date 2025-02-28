package com.akkorhotel.hotel.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class HotelLocation {

    @Id
    private String id;

    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    private String googleMapsUrl;
}
