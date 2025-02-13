package com.akkorhotel.hotel.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Getter
@Setter
@Builder
public class Booking {

    @Id
    private String id;

    private User user;
    private Hotel hotel;

    private Date checkInDate;
    private Date checkOutDate;

    private int guests;

    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    private String specialRequest;

}
