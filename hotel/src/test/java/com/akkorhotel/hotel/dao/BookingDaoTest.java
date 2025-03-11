package com.akkorhotel.hotel.dao;

import com.akkorhotel.hotel.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class BookingDaoTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private BookingDao bookingDao;

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection("BOOKING");
    }

    @Test
    void shouldSaveNewBooking() {
        // Arrange
        HotelLocation hotelLocation = HotelLocation.builder()
                .id("hotelLocationId")
                .address("address")
                .city("city")
                .state("state")
                .country("country")
                .postalCode("postalCode")
                .googleMapsUrl("googleMapsUrl")
                .build();

        HotelRoom hotelRoom = HotelRoom.builder()
                .id("hotelRoomId")
                .price(100.00)
                .maxOccupancy(5)
                .features(List.of(HotelRoomFeatures.FLAT_SCREEN_TV, HotelRoomFeatures.SAFE))
                .type(HotelRoomType.DELUXE)
                .build();

        Hotel hotel = Hotel.builder()
                .id("hotelId")
                .location(hotelLocation)
                .name("name")
                .rooms(List.of(hotelRoom))
                .amenities(List.of(HotelAmenities.BAR, HotelAmenities.WIFI))
                .picture_list(List.of("picture1", "picture2"))
                .description("description")
                .stars(4)
                .build();

        Booking booking = Booking.builder()
                .id("bookingId")
                .userId("userId")
                .status(BookingStatus.CONFIRMED)
                .isPaid(true)
                .totalPrice(300.00)
                .checkInDate(new Date(1677628800000L))
                .checkOutDate(new Date(1677715200000L))
                .guests(3)
                .hotelRoom(hotelRoom)
                .hotel(hotel)
                .build();

        // Act
        bookingDao.save(booking);

        // Assert
        List<Map> savedBooking = mongoTemplate.findAll(Map.class, "BOOKING");
        assertThat((Map<String, Object>) savedBooking.getFirst())
                .containsExactlyInAnyOrderEntriesOf(ofEntries(
                        entry("_id", "bookingId"),
                        entry("userId", "userId"),
                        entry("status", "CONFIRMED"),
                        entry("isPaid", true),
                        entry("totalPrice", 300.0),
                        entry("checkInDate", new Date(1677628800000L)),
                        entry("checkOutDate", new Date(1677715200000L)),
                        entry("guests", 3),
                        entry("hotelRoom", Map.ofEntries(
                                entry("_id", "hotelRoomId"),
                                entry("price", 100.0),
                                entry("maxOccupancy", 5),
                                entry("features", List.of("FLAT_SCREEN_TV", "SAFE")),
                                entry("type", "DELUXE")
                        )),
                        entry("hotel", Map.ofEntries(
                                entry("_id", "hotelId"),
                                entry("name", "name"),
                                entry("description", "description"),
                                entry("picture_list", List.of("picture1", "picture2")),
                                entry("amenities", List.of("BAR", "WIFI")),
                                entry("stars", 4),
                                entry("rooms", List.of(
                                        Map.ofEntries(
                                                entry("_id", "hotelRoomId"),
                                                entry("price", 100.0),
                                                entry("maxOccupancy", 5),
                                                entry("features", List.of("FLAT_SCREEN_TV", "SAFE")),
                                                entry("type", "DELUXE")
                                        )
                                )),
                                entry("location", Map.ofEntries(
                                        entry("_id", "hotelLocationId"),
                                        entry("address", "address"),
                                        entry("city", "city"),
                                        entry("state", "state"),
                                        entry("country", "country"),
                                        entry("postalCode", "postalCode"),
                                        entry("googleMapsUrl", "googleMapsUrl")
                                ))
                        ))
                ));
    }

    @Test
    void shouldReturnBooking_whenIdExistsInDatabase() {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "bookingId",
            "userId": "userId",
            "status": "PENDING",
            "isPaid": false,
            "totalPrice": 600.0,
            "checkInDate": { "$date": "2023-03-10T14:00:00.000Z" },
            "checkOutDate": { "$date": "2023-03-15T12:00:00.000Z" },
            "guests": 3,
            "hotelRoom": {
                "_id": "hotelRoomId",
                "price": 120.0,
                "maxOccupancy": 3,
                "features": ["ROOM_SERVICE", "BALCONY"],
                "type": "SINGLE"
            },
            "hotel": {
                "_id": "hotelId",
                "name": "name",
                "picture_list": ["https://example.com/pic1.jpg", "https://example.com/pic2.jpg"],
                "amenities": ["PARKING", "BAR"],
                "stars": 4,
                "rooms": [
                    {
                        "_id": "hotelRoomId1",
                        "price": 120.0,
                        "maxOccupancy": 3,
                        "features": ["ROOM_SERVICE", "BALCONY"],
                        "type": "SINGLE"
                    },
                    {
                        "_id": "hotelRoomId2",
                        "price": 150.0,
                        "maxOccupancy": 5,
                        "features": ["WIFI", "HAIR_DRYER"],
                        "type": "DOUBLE"
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
        }
        """, "BOOKING");

        // Act
        Optional<Booking> bookingOptional = bookingDao.findById("bookingId");

        // Assert
        assertThat(bookingOptional).isPresent();
    }

    @Test
    void shouldReturnEmptyOptional_whenIdDoesNotExistInDatabase() {
        // Act
        Optional<Booking> bookingOptional = bookingDao.findById("nonexistent_bookingId");

        // Assert
        assertThat(bookingOptional).isEmpty();
    }

    @Test
    void shouldDeleteBooking() {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "bookingId",
            "userId": "userId",
            "status": "PENDING",
            "isPaid": false,
            "totalPrice": 600.0,
            "checkInDate": { "$date": "2023-03-10T14:00:00.000Z" },
            "checkOutDate": { "$date": "2023-03-15T12:00:00.000Z" },
            "guests": 3,
            "hotelRoom": {
                "_id": "hotelRoomId",
                "price": 120.0,
                "maxOccupancy": 3,
                "features": ["ROOM_SERVICE", "BALCONY"],
                "type": "SINGLE"
            },
            "hotel": {
                "_id": "hotelId",
                "name": "name",
                "picture_list": ["https://example.com/pic1.jpg", "https://example.com/pic2.jpg"],
                "amenities": ["PARKING", "BAR"],
                "stars": 4,
                "rooms": [
                    {
                        "_id": "hotelRoomId1",
                        "price": 120.0,
                        "maxOccupancy": 3,
                        "features": ["ROOM_SERVICE", "BALCONY"],
                        "type": "SINGLE"
                    },
                    {
                        "_id": "hotelRoomId2",
                        "price": 150.0,
                        "maxOccupancy": 5,
                        "features": ["WIFI", "HAIR_DRYER"],
                        "type": "DOUBLE"
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
        }
        """, "BOOKING");

        // Act
        bookingDao.delete("bookingId");

        // Assert
        List<Booking> savedBookings = mongoTemplate.findAll(Booking.class, "BOOKING");

        assertThat(savedBookings).isEmpty();
    }

    @Test
    void shouldReturnBookingsWithMatchingUserId() {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "bookingId1",
            "userId": "userId1",
            "status": "PENDING",
            "isPaid": false,
            "totalPrice": 600.0,
            "checkInDate": { "$date": "2023-03-10T14:00:00.000Z" },
            "checkOutDate": { "$date": "2023-03-15T12:00:00.000Z" },
            "guests": 3,
            "hotelRoom": {
                "_id": "hotelRoomId",
                "price": 120.0,
                "maxOccupancy": 3,
                "features": ["ROOM_SERVICE", "BALCONY"],
                "type": "SINGLE"
            },
            "hotel": {
                "_id": "hotelId",
                "name": "name",
                "picture_list": ["https://example.com/pic1.jpg", "https://example.com/pic2.jpg"],
                "amenities": ["PARKING", "BAR"],
                "stars": 4,
                "rooms": [
                    {
                        "_id": "hotelRoomId",
                        "price": 120.0,
                        "maxOccupancy": 3,
                        "features": ["ROOM_SERVICE", "BALCONY"],
                        "type": "SINGLE"
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
        }
        """, "BOOKING");

        mongoTemplate.insert("""
        {
            "_id": "bookingId2",
            "userId": "userId2",
            "status": "PENDING",
            "isPaid": false,
            "totalPrice": 600.0,
            "checkInDate": { "$date": "2023-03-10T14:00:00.000Z" },
            "checkOutDate": { "$date": "2023-03-15T12:00:00.000Z" },
            "guests": 3,
            "hotelRoom": {
                "_id": "hotelRoomId",
                "price": 120.0,
                "maxOccupancy": 3,
                "features": ["ROOM_SERVICE", "BALCONY"],
                "type": "SINGLE"
            },
            "hotel": {
                "_id": "hotelId",
                "name": "name",
                "picture_list": ["https://example.com/pic1.jpg", "https://example.com/pic2.jpg"],
                "amenities": ["PARKING", "BAR"],
                "stars": 4,
                "rooms": [
                    {
                        "_id": "hotelRoomId1",
                        "price": 120.0,
                        "maxOccupancy": 3,
                        "features": ["ROOM_SERVICE", "BALCONY"],
                        "type": "SINGLE"
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
        }
        """, "BOOKING");

        // Act
        List<Booking> bookings = bookingDao.getBookings("userId1");

        // Assert
        HotelLocation hotelLocation = HotelLocation.builder()
                .id("locationId")
                .address("address")
                .city("city")
                .state("state")
                .country("country")
                .postalCode("postalCode")
                .googleMapsUrl("googleMapsUrl")
                .build();

        HotelRoom hotelRoom = HotelRoom.builder()
                .id("hotelRoomId")
                .price(120.00)
                .maxOccupancy(3)
                .features(List.of(HotelRoomFeatures.ROOM_SERVICE, HotelRoomFeatures.BALCONY))
                .type(HotelRoomType.SINGLE)
                .build();

        Hotel hotel = Hotel.builder()
                .id("hotelId")
                .location(hotelLocation)
                .name("name")
                .rooms(List.of(hotelRoom))
                .amenities(List.of(HotelAmenities.PARKING, HotelAmenities.BAR))
                .picture_list(List.of("https://example.com/pic1.jpg", "https://example.com/pic2.jpg"))
                .stars(4)
                .build();

        Booking booking = Booking.builder()
                .id("bookingId1")
                .userId("userId1")
                .status(BookingStatus.PENDING)
                .isPaid(false)
                .totalPrice(600.00)
                .checkInDate(new Date(1678456800000L))
                .checkOutDate(new Date(1678881600000L))
                .guests(3)
                .hotelRoom(hotelRoom)
                .hotel(hotel)
                .build();

        assertThat(bookings).isEqualTo(List.of(booking));
    }

    @Test
    void shouldReturnBookingsWithMatchingHotelId() {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "bookingId1",
            "userId": "userId1",
            "status": "PENDING",
            "isPaid": false,
            "totalPrice": 600.0,
            "checkInDate": { "$date": "2023-03-10T14:00:00.000Z" },
            "checkOutDate": { "$date": "2023-03-15T12:00:00.000Z" },
            "guests": 3,
            "hotelRoom": {
                "_id": "hotelRoomId",
                "price": 120.0,
                "maxOccupancy": 3,
                "features": ["ROOM_SERVICE", "BALCONY"],
                "type": "SINGLE"
            },
            "hotel": {
                "_id": "hotelId1",
                "name": "name",
                "picture_list": ["https://example.com/pic1.jpg", "https://example.com/pic2.jpg"],
                "amenities": ["PARKING", "BAR"],
                "stars": 4,
                "rooms": [
                    {
                        "_id": "hotelRoomId",
                        "price": 120.0,
                        "maxOccupancy": 3,
                        "features": ["ROOM_SERVICE", "BALCONY"],
                        "type": "SINGLE"
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
        }
        """, "BOOKING");

        mongoTemplate.insert("""
        {
            "_id": "bookingId2",
            "userId": "userId2",
            "status": "PENDING",
            "isPaid": false,
            "totalPrice": 600.0,
            "checkInDate": { "$date": "2023-03-10T14:00:00.000Z" },
            "checkOutDate": { "$date": "2023-03-15T12:00:00.000Z" },
            "guests": 3,
            "hotelRoom": {
                "_id": "hotelRoomId",
                "price": 120.0,
                "maxOccupancy": 3,
                "features": ["ROOM_SERVICE", "BALCONY"],
                "type": "SINGLE"
            },
            "hotel": {
                "_id": "hotelId2",
                "name": "name",
                "picture_list": ["https://example.com/pic1.jpg", "https://example.com/pic2.jpg"],
                "amenities": ["PARKING", "BAR"],
                "stars": 4,
                "rooms": [
                    {
                        "_id": "hotelRoomId1",
                        "price": 120.0,
                        "maxOccupancy": 3,
                        "features": ["ROOM_SERVICE", "BALCONY"],
                        "type": "SINGLE"
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
        }
        """, "BOOKING");

        // Act
        List<Booking> bookings = bookingDao.getHotelBookings("hotelId1");

        // Assert
        HotelLocation hotelLocation = HotelLocation.builder()
                .id("locationId")
                .address("address")
                .city("city")
                .state("state")
                .country("country")
                .postalCode("postalCode")
                .googleMapsUrl("googleMapsUrl")
                .build();

        HotelRoom hotelRoom = HotelRoom.builder()
                .id("hotelRoomId")
                .price(120.00)
                .maxOccupancy(3)
                .features(List.of(HotelRoomFeatures.ROOM_SERVICE, HotelRoomFeatures.BALCONY))
                .type(HotelRoomType.SINGLE)
                .build();

        Hotel hotel = Hotel.builder()
                .id("hotelId1")
                .location(hotelLocation)
                .name("name")
                .rooms(List.of(hotelRoom))
                .amenities(List.of(HotelAmenities.PARKING, HotelAmenities.BAR))
                .picture_list(List.of("https://example.com/pic1.jpg", "https://example.com/pic2.jpg"))
                .stars(4)
                .build();

        Booking booking = Booking.builder()
                .id("bookingId1")
                .userId("userId1")
                .status(BookingStatus.PENDING)
                .isPaid(false)
                .totalPrice(600.00)
                .checkInDate(new Date(1678456800000L))
                .checkOutDate(new Date(1678881600000L))
                .guests(3)
                .hotelRoom(hotelRoom)
                .hotel(hotel)
                .build();

        assertThat(bookings).isEqualTo(List.of(booking));
    }

}