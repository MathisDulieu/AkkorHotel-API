package com.akkorhotel.hotel.dao;

import com.akkorhotel.hotel.model.Hotel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HotelDao {

    private final MongoTemplate mongoTemplate;

    private static final String HOTEL_COLLECTION = "HOTELS";

    public void save(Hotel hotel) {
        mongoTemplate.save(hotel, HOTEL_COLLECTION);
    }

    public Optional<Hotel> findById(String hotelId) {
        return Optional.ofNullable(mongoTemplate.findById(hotelId, Hotel.class, HOTEL_COLLECTION));
    }

    public void delete(String hotelId) {
        mongoTemplate.remove(new Query(Criteria.where("_id").is(hotelId)), HOTEL_COLLECTION);
    }
}
