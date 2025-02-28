package com.akkorhotel.hotel.dao;

import com.akkorhotel.hotel.model.Hotel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotelDao {

    private final MongoTemplate mongoTemplate;

    private static final String HOTEL_COLLECTION = "HOTELS";

    public void save(Hotel hotel) {
        mongoTemplate.save(hotel, HOTEL_COLLECTION);
    }

}
