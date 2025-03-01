package com.akkorhotel.hotel.dao;

import com.akkorhotel.hotel.model.HotelRoom;
import com.akkorhotel.hotel.model.HotelRoomFeatures;
import com.akkorhotel.hotel.model.HotelRoomType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class HotelRoomDaoTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HotelRoomDao hotelRoomDao;

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection("HOTEL_ROOMS");
    }

    @Test
    void shouldSaveNewHotelRoom() {
        // Arrange
        HotelRoom hotelRoom = HotelRoom.builder()
                .id("id")
                .type(HotelRoomType.DELUXE)
                .maxOccupancy(6)
                .price(200)
                .features(List.of(HotelRoomFeatures.FLAT_SCREEN_TV, HotelRoomFeatures.MINI_FRIDGE))
                .build();

        // Act
        hotelRoomDao.save(hotelRoom);

        // Assert
        List<Map> savedHotelRooms = mongoTemplate.findAll(Map.class, "HOTEL_ROOMS");
        assertThat((Map<String, Object>) savedHotelRooms.getFirst())
                .containsExactlyInAnyOrderEntriesOf(ofEntries(
                        entry("_id", "id"),
                        entry("type", "DELUXE"),
                        entry("maxOccupancy", 6),
                        entry("price", 200.0),
                        entry("features", List.of("FLAT_SCREEN_TV", "MINI_FRIDGE"))
                ));
    }

}