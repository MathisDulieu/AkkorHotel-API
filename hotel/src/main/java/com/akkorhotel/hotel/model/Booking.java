package com.akkorhotel.hotel.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class Booking {

    @Id
    private String id;

    private String userId;
    private Hotel hotel;
    private HotelRoom hotelRoom;

    private Date checkInDate;
    private Date checkOutDate;

    private int guests;
    private boolean isPaid;
    private double totalPrice;

    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

}
