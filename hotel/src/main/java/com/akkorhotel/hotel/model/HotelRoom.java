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
public class HotelRoom {

    @Id
    private String id;
    private HotelRoomType type;
    private double price;
    private int maxOccupancy;

    @Builder.Default
    private List<HotelRoomFeatures> features = emptyList();

}
