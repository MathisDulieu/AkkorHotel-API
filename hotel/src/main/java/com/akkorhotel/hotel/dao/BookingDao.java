package com.akkorhotel.hotel.dao;

import com.akkorhotel.hotel.model.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingDao {

    private final MongoTemplate mongoTemplate;

    private static final String BOOKING_COLLECTION = "BOOKING";

    public void save(Booking booking) {
        mongoTemplate.save(booking, BOOKING_COLLECTION);
    }
}
