package com.akkorhotel.hotel.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RemovePictureFromHotelRequest {
    private String pictureLink;
}
