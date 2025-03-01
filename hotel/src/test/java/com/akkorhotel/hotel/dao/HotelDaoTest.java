package com.akkorhotel.hotel.dao;

import com.akkorhotel.hotel.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    void shouldSaveNewHotel() {
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

    @Test
    void shouldReturnHotel_whenIdExistsInDatabase() {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "hotelId",
            "name": "name",
            "picture_list": ["picture1", "picture2"],
            "amenities": ["PARKING", "BAR"],
            "rooms": [
                {
                    "_id": "roomId1",
                    "type": "SINGLE",
                    "price": 120,
                    "maxOccupancy": 3,
                    "features": ["ROOM_SERVICE", "BALCONY"]
                },
                {
                    "_id": "roomId2",
                    "type": "DOUBLE",
                    "price": 150,
                    "maxOccupancy": 5,
                    "features": ["WIFI", "HAIR_DRYER"]
                }
            ],
            "location": {
                "_id": "locationId",
                "address": "address",
                "city": "city",
                "state": "state",
                "country": "country",
                "postalCode": "postalCode",
                "googleMapsUrl": "googleMapsUrl"
            }
        }
        """, "HOTELS");

        // Act
        Optional<Hotel> hotelOptional = hotelDao.findById("hotelId");

        // Assert
        assertThat(hotelOptional).isPresent();
        assertThat(hotelOptional.get().getId()).isEqualTo("hotelId");
        assertThat(hotelOptional.get().getName()).isEqualTo("name");
        assertThat(hotelOptional.get().getPicture_list()).isEqualTo(List.of("picture1", "picture2"));
        assertThat(hotelOptional.get().getAmenities()).isEqualTo(List.of(HotelAmenities.PARKING, HotelAmenities.BAR));
        assertThat(hotelOptional.get().getRooms()).isEqualTo(List.of(
                HotelRoom.builder()
                        .id("roomId1")
                        .type(HotelRoomType.SINGLE)
                        .price(120)
                        .maxOccupancy(3)
                        .features(List.of(HotelRoomFeatures.ROOM_SERVICE, HotelRoomFeatures.BALCONY))
                        .build(),
                HotelRoom.builder()
                        .id("roomId2")
                        .type(HotelRoomType.DOUBLE)
                        .price(150)
                        .maxOccupancy(5)
                        .features(List.of(HotelRoomFeatures.WIFI, HotelRoomFeatures.HAIR_DRYER))
                        .build()
                ));
        assertThat(hotelOptional.get().getLocation()).isEqualTo(HotelLocation.builder()
                .id("locationId")
                .address("address")
                .city("city")
                .state("state")
                .country("country")
                .postalCode("postalCode")
                .googleMapsUrl("googleMapsUrl")
                .build()
        );
    }

    @Test
    void shouldReturnEmptyOptional_whenIdDoesNotExistInDatabase() {
        // Act
        Optional<Hotel> hotelOptional = hotelDao.findById("nonexistent_hotelId");

        // Assert
        assertThat(hotelOptional).isEmpty();
    }

}