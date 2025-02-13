package com.akkorhotel.hotel.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingDao {

    private final MongoTemplate mongoTemplate;

    private static final String BOOKING_COLLECTION = "BOOKING";

}
