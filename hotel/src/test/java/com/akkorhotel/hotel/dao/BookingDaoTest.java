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
    void shouldSaveNewHotel() {
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

}