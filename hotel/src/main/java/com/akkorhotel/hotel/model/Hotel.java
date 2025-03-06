package com.akkorhotel.hotel.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.List;

import static java.util.Collections.emptyList;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class Hotel {

    @Id
    private String id;

    private String name;
    private String description;

    private HotelLocation location;

    @Builder.Default
    private List<String> picture_list = emptyList();

    @Builder.Default
    private List<HotelAmenities> amenities = emptyList();

    @Builder.Default
    private List<HotelRoom> rooms = emptyList();

    private int stars;

}
