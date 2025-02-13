package com.akkorhotel.hotel.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotelDao {

    private final MongoTemplate mongoTemplate;

    private static final String HOTEL_COLLECTION = "HOTELS";

}
