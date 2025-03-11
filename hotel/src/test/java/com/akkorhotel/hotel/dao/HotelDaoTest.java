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
                .stars(4)
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
                        entry("stars", 4),
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
            "stars": 4,
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
        assertThat(hotelOptional.get().getStars()).isEqualTo(4);
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

    @Test
    void shouldCountHotelsByNamePrefix() {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "hotelId1",
            "name": "name1",
            "picture_list": ["picture1", "picture2"],
            "amenities": ["PARKING", "BAR"],
            "rooms": [],
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
            "rooms": [],
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

        mongoTemplate.insert("""
        {
            "_id": "hotelId3",
            "name": "anotherName",
            "picture_list": ["picture5", "picture6"],
            "amenities": ["RESTAURANT", "WIFI"],
            "rooms": [],
            "location": {
                "_id": "locationId3",
                "address": "address3",
                "city": "city3",
                "state": "state3",
                "country": "country3",
                "postalCode": "postalCode3",
                "googleMapsUrl": "googleMapsUrl3"
            }
        }
        """, "HOTELS");

        // Act
        long count = hotelDao.countHotelsByNamePrefix("name");

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldReturnZero_whenNoHotelExist() {
        // Act
        long count = hotelDao.countHotelsByNamePrefix("any");

        // Assert
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldReturnAllHotelsCount_whenEmptyPrefixIsSet() {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "hotelId1",
            "name": "name1",
            "picture_list": ["picture1", "picture2"],
            "amenities": ["PARKING", "BAR"],
            "rooms": [],
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
            "rooms": [],
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

        mongoTemplate.insert("""
        {
            "_id": "hotelId3",
            "name": "anotherName",
            "picture_list": ["picture5", "picture6"],
            "amenities": ["RESTAURANT", "WIFI"],
            "rooms": [],
            "location": {
                "_id": "locationId3",
                "address": "address3",
                "city": "city3",
                "state": "state3",
                "country": "country3",
                "postalCode": "postalCode3",
                "googleMapsUrl": "googleMapsUrl3"
            }
        }
        """, "HOTELS");

        // Act
        long count = hotelDao.countHotelsByNamePrefix("");

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldReturnHotelsMatchingPrefix() {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "hotelId1",
            "name": "name1",
            "picture_list": ["picture1", "picture2"],
            "amenities": ["PARKING", "BAR"],
            "stars": 4,
            "rooms": [],
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
            "stars": 4,
            "rooms": [],
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

        mongoTemplate.insert("""
        {
            "_id": "hotelId3",
            "name": "anotherName",
            "picture_list": ["picture5", "picture6"],
            "amenities": ["RESTAURANT", "WIFI"],
            "stars": 4,
            "rooms": [],
            "location": {
                "_id": "locationId3",
                "address": "address3",
                "city": "city3",
                "state": "state3",
                "country": "country3",
                "postalCode": "postalCode3",
                "googleMapsUrl": "googleMapsUrl3"
            }
        }
        """, "HOTELS");

        // Act
        List<Hotel> hotels = hotelDao.searchHotelsByNamePrefix("name", 0, 10);

        // Assert
        assertThat(hotels).hasSize(2);
        assertThat(hotels).containsExactlyInAnyOrder(
                Hotel.builder()
                        .id("hotelId1")
                        .name("name1")
                        .picture_list(List.of("picture1", "picture2"))
                        .amenities(List.of(HotelAmenities.PARKING, HotelAmenities.BAR))
                        .stars(4)
                        .rooms(emptyList())
                        .location(HotelLocation.builder()
                                .id("locationId1")
                                .address("address1")
                                .city("city1")
                                .state("state1")
                                .country("country1")
                                .postalCode("postalCode1")
                                .googleMapsUrl("googleMapsUrl1")
                                .build())
                        .build(),
                Hotel.builder()
                        .id("hotelId2")
                        .name("name2")
                        .picture_list(List.of("picture3", "picture4"))
                        .amenities(List.of(HotelAmenities.RESTAURANT, HotelAmenities.WIFI))
                        .stars(4)
                        .rooms(emptyList())
                        .location(HotelLocation.builder()
                                .id("locationId2")
                                .address("address2")
                                .city("city2")
                                .state("state2")
                                .country("country2")
                                .postalCode("postalCode2")
                                .googleMapsUrl("googleMapsUrl2")
                                .build())
                        .build()
        );
    }

    @Test
    void shouldReturnPaginatedResults() {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "hotelId1",
            "name": "name1",
            "picture_list": ["picture1", "picture2"],
            "amenities": ["PARKING", "BAR"],
            "stars": 4,
            "rooms": [],
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
            "stars": 4,
            "rooms": [],
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

        mongoTemplate.insert("""
        {
            "_id": "hotelId3",
            "name": "name3",
            "picture_list": ["picture5", "picture6"],
            "amenities": ["RESTAURANT", "WIFI"],
            "stars": 4,
            "rooms": [],
            "location": {
                "_id": "locationId3",
                "address": "address3",
                "city": "city3",
                "state": "state3",
                "country": "country3",
                "postalCode": "postalCode3",
                "googleMapsUrl": "googleMapsUrl3"
            }
        }
        """, "HOTELS");

        mongoTemplate.insert("""
        {
            "_id": "hotelId4",
            "name": "name4",
            "picture_list": ["picture7", "picture8"],
            "amenities": ["RESTAURANT", "WIFI"],
            "stars": 4,
            "rooms": [],
            "location": {
                "_id": "locationId4",
                "address": "address4",
                "city": "city4",
                "state": "state4",
                "country": "country4",
                "postalCode": "postalCode4",
                "googleMapsUrl": "googleMapsUrl4"
            }
        }
        """, "HOTELS");

        // Act
        List<Hotel> hotels = hotelDao.searchHotelsByNamePrefix("name", 0, 2);

        // Assert
        assertThat(hotels).hasSize(2);
        assertThat(hotels).containsExactlyInAnyOrder(
                Hotel.builder()
                        .id("hotelId1")
                        .name("name1")
                        .picture_list(List.of("picture1", "picture2"))
                        .amenities(List.of(HotelAmenities.PARKING, HotelAmenities.BAR))
                        .stars(4)
                        .rooms(emptyList())
                        .location(HotelLocation.builder()
                                .id("locationId1")
                                .address("address1")
                                .city("city1")
                                .state("state1")
                                .country("country1")
                                .postalCode("postalCode1")
                                .googleMapsUrl("googleMapsUrl1")
                                .build())
                        .build(),
                Hotel.builder()
                        .id("hotelId2")
                        .name("name2")
                        .picture_list(List.of("picture3", "picture4"))
                        .amenities(List.of(HotelAmenities.RESTAURANT, HotelAmenities.WIFI))
                        .stars(4)
                        .rooms(emptyList())
                        .location(HotelLocation.builder()
                                .id("locationId2")
                                .address("address2")
                                .city("city2")
                                .state("state2")
                                .country("country2")
                                .postalCode("postalCode2")
                                .googleMapsUrl("googleMapsUrl2")
                                .build())
                        .build()
        );
    }

    @Test
    void shouldReturnHotelsSortedAlphabetically() {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "hotelId1",
            "name": "nameA2",
            "picture_list": ["picture1", "picture2"],
            "amenities": ["PARKING", "BAR"],
            "stars": 4,
            "rooms": [],
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
            "name": "nameA1",
            "picture_list": ["picture3", "picture4"],
            "amenities": ["RESTAURANT", "WIFI"],
            "stars": 4,
            "rooms": [],
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
        List<Hotel> hotels = hotelDao.searchHotelsByNamePrefix("name", 0, 10);

        // Assert
        assertThat(hotels).containsExactly(
                Hotel.builder()
                        .id("hotelId2")
                        .name("nameA1")
                        .picture_list(List.of("picture3", "picture4"))
                        .amenities(List.of(HotelAmenities.RESTAURANT, HotelAmenities.WIFI))
                        .stars(4)
                        .rooms(emptyList())
                        .location(HotelLocation.builder()
                                .id("locationId2")
                                .address("address2")
                                .city("city2")
                                .state("state2")
                                .country("country2")
                                .postalCode("postalCode2")
                                .googleMapsUrl("googleMapsUrl2")
                                .build())
                        .build(),
                Hotel.builder()
                        .id("hotelId1")
                        .name("nameA2")
                        .picture_list(List.of("picture1", "picture2"))
                        .amenities(List.of(HotelAmenities.PARKING, HotelAmenities.BAR))
                        .stars(4)
                        .rooms(emptyList())
                        .location(HotelLocation.builder()
                                .id("locationId1")
                                .address("address1")
                                .city("city1")
                                .state("state1")
                                .country("country1")
                                .postalCode("postalCode1")
                                .googleMapsUrl("googleMapsUrl1")
                                .build())
                        .build()
        );
    }

    @Test
    void shouldReturnEmptyList_whenNoMatchingHotel() {
        // Act
        List<Hotel> hotels = hotelDao.searchHotelsByNamePrefix("name", 0, 10);

        // Assert
        assertThat(hotels).isEmpty();
    }

    @Test
    void shouldReturnAllHotels_whenEmptyPrefixIsSet() {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "hotelId1",
            "name": "name1",
            "picture_list": ["picture1", "picture2"],
            "amenities": ["PARKING", "BAR"],
            "stars": 3,
            "rooms": [],
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
            "stars": 4,
            "rooms": [],
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
        List<Hotel> hotels = hotelDao.searchHotelsByNamePrefix("", 0, 10);

        // Assert
        assertThat(hotels).hasSize(2);
        assertThat(hotels).containsExactlyInAnyOrder(
                Hotel.builder()
                        .id("hotelId1")
                        .name("name1")
                        .picture_list(List.of("picture1", "picture2"))
                        .amenities(List.of(HotelAmenities.PARKING, HotelAmenities.BAR))
                        .stars(3)
                        .rooms(emptyList())
                        .location(HotelLocation.builder()
                                .id("locationId1")
                                .address("address1")
                                .city("city1")
                                .state("state1")
                                .country("country1")
                                .postalCode("postalCode1")
                                .googleMapsUrl("googleMapsUrl1")
                                .build())
                        .build(),
                Hotel.builder()
                        .id("hotelId2")
                        .name("name2")
                        .picture_list(List.of("picture3", "picture4"))
                        .amenities(List.of(HotelAmenities.RESTAURANT, HotelAmenities.WIFI))
                        .stars(4)
                        .rooms(emptyList())
                        .location(HotelLocation.builder()
                                .id("locationId2")
                                .address("address2")
                                .city("city2")
                                .state("state2")
                                .country("country2")
                                .postalCode("postalCode2")
                                .googleMapsUrl("googleMapsUrl2")
                                .build())
                        .build()
        );
    }

    @Test
    void shouldReturnFalse_whenHotelDoesNotExist() {
        // Arrange
        // Act
        boolean exist = hotelDao.exists("hotelId");

        // Assert
        assertThat(exist).isEqualTo(false);
    }

    @Test
    void shouldReturnTrue_whenUserExist() {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "hotelId",
            "name": "name",
            "picture_list": ["picture1", "picture2"],
            "amenities": ["PARKING", "BAR"],
            "stars": 3,
            "rooms": [],
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

        // Act
        boolean exist = hotelDao.exists("hotelId");

        // Assert
        assertThat(exist).isEqualTo(true);
    }

}