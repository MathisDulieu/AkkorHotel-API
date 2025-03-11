package com.akkorhotel.hotel.model.response;

import com.akkorhotel.hotel.model.Booking;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AdminGetBookingsResponse {
    private List<Booking> bookings;
    private String error;
}
