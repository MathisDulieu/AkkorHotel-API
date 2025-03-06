package com.akkorhotel.hotel.model.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GetAllHotelsHotelResponse {
    private String hotelId;
    private String firstPicture;
    private String name;
    private String description;
    private String address;
    private String googleMapUrl;
    private double price;
    private int stars;
}
