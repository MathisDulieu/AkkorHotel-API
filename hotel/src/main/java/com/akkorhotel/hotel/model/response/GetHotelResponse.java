package com.akkorhotel.hotel.model.response;

import com.akkorhotel.hotel.model.Hotel;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GetHotelResponse {
    private Hotel hotel;
    private String error;
}
