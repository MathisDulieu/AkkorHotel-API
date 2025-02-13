package com.akkorhotel.hotel.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.List;

import static java.util.Collections.emptyList;

@Getter
@Setter
@Builder
public class Hotel {

    @Id
    private String id;

    private String name;
    private String description;

    @Builder.Default
    private HotelLocation location = HotelLocation.builder().build();

    @Builder.Default
    private List<String> picture_list = emptyList();

}
