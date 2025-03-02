package com.akkorhotel.hotel.dao;

import com.akkorhotel.hotel.model.HotelRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HotelRoomDao {

    private final MongoTemplate mongoTemplate;

    private static final String HOTEL_ROOM_COLLECTION = "HOTEL_ROOMS";

    public void save(HotelRoom hotelRoom) {
        mongoTemplate.save(hotelRoom, HOTEL_ROOM_COLLECTION);
    }

    public Optional<HotelRoom> findById(String hotelRoomId) {
        return Optional.ofNullable(mongoTemplate.findById(hotelRoomId, HotelRoom.class, HOTEL_ROOM_COLLECTION));
    }
    public void delete(String hotelRoomId) {
        mongoTemplate.remove(new Query(Criteria.where("_id").is(hotelRoomId)), HOTEL_ROOM_COLLECTION);
    }

}
