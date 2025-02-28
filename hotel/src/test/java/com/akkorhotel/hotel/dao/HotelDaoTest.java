package com.akkorhotel.hotel.dao;

import com.akkorhotel.hotel.model.Hotel;
import com.akkorhotel.hotel.model.HotelLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class HotelDaoTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HotelDao hotelDao;

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection("HOTELS");
    }

    @Test
    void shouldSaveNewUser() {
        // Arrange
        Hotel hotel = Hotel.builder()
                .id("f2cccd2f-5711-4356-a13a-f687dc983ce0")
                .location(HotelLocation.builder()
                        .id("f2cccd2f-5711-4356-a13a-f687dc983ce1")
                        .address("address")
                        .city("city")
                        .state("state")
                        .country("country")
                        .postalCode("postalCode")
                        .googleMapsUrl("googleMapsUrl")
                        .build())
                .name("name")
                .build();

        // Act
        hotelDao.save(hotel);

        // Assert
        List<Map> savedHotels = mongoTemplate.findAll(Map.class, "HOTELS");
        assertThat((Map<String, Object>) savedHotels.getFirst())
                .containsExactlyInAnyOrderEntriesOf(ofEntries(
                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce0"),
                        entry("name", "name"),
                        entry("picture_list", emptyList()),
                        entry("amenities", emptyList()),
                        entry("rooms", emptyList()),
                        entry("location", Map.of(
                                "_id", "f2cccd2f-5711-4356-a13a-f687dc983ce1",
                                "address", "address",
                                "city", "city",
                                "state", "state",
                                "country", "country",
                                "postalCode", "postalCode",
                                "googleMapsUrl", "googleMapsUrl"
                        ))
                ));
    }
}