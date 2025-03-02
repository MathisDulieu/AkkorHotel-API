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

    @Test
    void shouldDeleteHotel() {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "hotelId1",
            "name": "name1",
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
                "_id": "locationId1",
                "address": "address1",
                "city": "city1",
                "state": "state1",
                "country": "country1",
                "postalCode": "postalCode1",
                "googleMapsUrl": "googleMapsUrl1"
            }
        }
        """, "HOTELS");

        mongoTemplate.insert("""
        {
            "_id": "hotelId2",
            "name": "name2",
            "picture_list": ["picture3", "picture4"],
            "amenities": ["RESTAURANT", "WIFI"],
            "rooms": [
                {
                    "_id": "roomId3",
                    "type": "PENTHOUSE",
                    "price": 200,
                    "maxOccupancy": 8,
                    "features": ["MINI_FRIDGE", "DESK"]
                },
                {
                    "_id": "roomId4",
                    "type": "FAMILY",
                    "price": 250,
                    "maxOccupancy": 11,
                    "features": ["PET_FRIENDLY", "NO_SMOKING"]
                }
            ],
            "location": {
                "_id": "locationId2",
                "address": "address2",
                "city": "city2",
                "state": "state2",
                "country": "country2",
                "postalCode": "postalCode2",
                "googleMapsUrl": "googleMapsUrl2"
            }
        }
        """, "HOTELS");

        // Act
        hotelDao.delete("hotelId1");

        // Assert
        List<Map> savedHotels = mongoTemplate.findAll(Map.class, "HOTELS");
        assertThat(savedHotels).hasSize(1);
        assertThat((Map<String, Object>) savedHotels.getFirst())
                .containsExactlyInAnyOrderEntriesOf(ofEntries(
                        entry("_id", "hotelId2"),
                        entry("name", "name2"),
                        entry("picture_list", List.of("picture3", "picture4")),
                        entry("amenities", List.of("RESTAURANT", "WIFI")),
                        entry("rooms", List.of(
                                Map.ofEntries(
                                        entry("_id", "roomId3"),
                                        entry("type", "PENTHOUSE"),
                                        entry("price", 200),
                                        entry("maxOccupancy", 8),
                                        entry("features", List.of("MINI_FRIDGE", "DESK"))
                                ),
                                Map.ofEntries(
                                        entry("_id", "roomId4"),
                                        entry("type", "FAMILY"),
                                        entry("price", 250),
                                        entry("maxOccupancy", 11),
                                        entry("features", List.of("PET_FRIENDLY", "NO_SMOKING"))
                                )
                        )),
                        entry("location", Map.ofEntries(
                                entry("_id", "locationId2"),
                                entry("address", "address2"),
                                entry("city", "city2"),
                                entry("state", "state2"),
                                entry("country", "country2"),
                                entry("postalCode", "postalCode2"),
                                entry("googleMapsUrl", "googleMapsUrl2")
                        ))
                ));
    }

}