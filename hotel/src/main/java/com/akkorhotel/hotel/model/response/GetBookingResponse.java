package com.akkorhotel.hotel.model.response;

import com.akkorhotel.hotel.model.Booking;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GetBookingResponse {
    private String error;
    private Booking booking;
}
