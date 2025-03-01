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
public class CreateHotelRoomRequest {

    private String hotelId;
    private String type;
    private double price;
    private int maxOccupancy;
    private List<String> features;

}
