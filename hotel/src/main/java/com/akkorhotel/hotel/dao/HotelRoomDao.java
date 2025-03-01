package com.akkorhotel.hotel.dao;

import com.akkorhotel.hotel.model.HotelRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotelRoomDao {

    private final MongoTemplate mongoTemplate;

    private static final String HOTEL_ROOM_COLLECTION = "HOTEL_ROOMS";

    public void save(HotelRoom hotelRoom) {
        mongoTemplate.save(hotelRoom, HOTEL_ROOM_COLLECTION);
    }

}
