package com.akkorhotel.hotel;

import com.akkorhotel.hotel.configuration.DateConfiguration;
import com.akkorhotel.hotel.model.Booking;
import com.akkorhotel.hotel.service.JwtTokenService;
import com.akkorhotel.hotel.service.UuidProvider;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@WireMockTest(httpPort = 8089)
public class BookingIntegrationTests {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UuidProvider uuidProvider;

    @MockBean
    private DateConfiguration dateConfiguration;

    private String token;

    @BeforeEach
    void setUp() {
        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce9",
                    "username": "username",
                    "password": "password",
                    "email": "email",
                    "isValidEmail": true,
                    "role": "USER",
                    "profileImageUrl": "https://profile-image.jpg"
                }
                """, "USERS");

        token = Jwts.builder()
                .setSubject("f2cccd2f-5711-4356-a13a-f687dc983ce9")
                .claim("type", "access")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(172_800_000)))
                .signWith(JwtTokenService.SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection("USERS");
        mongoTemplate.dropCollection("BOOKING");
        mongoTemplate.dropCollection("HOTELS");
        mongoTemplate.dropCollection("HOTEL_ROOMS");
    }

    @Test
    void shouldCreateBookingSuccessfully() throws Exception {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
            "name": "HotelParadise",
            "picture_list": ["https://example.com/pic1.jpg", "https://example.com/pic2.jpg"],
            "amenities": ["PARKING", "BAR", "POOL", "GYM"],
            "stars": 4,
            "rooms": [
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce2",
                    "type": "SINGLE",
                    "price": 120,
                    "maxOccupancy": 3,
                    "features": ["ROOM_SERVICE", "BALCONY"]
                },
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce3",
                    "type": "DOUBLE",
                    "price": 150,
                    "maxOccupancy": 5,
                    "features": ["WIFI", "HAIR_DRYER"]
                }
            ],
            "location": {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce4",
                "address": "123 Paradise St",
                "city": "Paradise City",
                "state": "California",
                "country": "USA",
                "postalCode": "90210",
                "googleMapsUrl": "https://maps.google.com/?q=123+Paradise+St"
            }
        }
        """, "HOTELS");

        mongoTemplate.insert("""
        {
            "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce2",
            "type": "SINGLE",
            "price": 120,
            "maxOccupancy": 3,
            "features": ["ROOM_SERVICE", "BALCONY"]
        }
        """, "HOTEL_ROOMS");

        mongoTemplate.insert("""
        {
            "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce3",
            "type": "DOUBLE",
            "price": 150,
            "maxOccupancy": 5,
            "features": ["WIFI", "HAIR_DRYER"]
        }
        """, "HOTEL_ROOMS");

        String body = """
        {
            "hotelId": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
            "hotelRoomId": "f2cccd2f-5711-4356-a13a-f687dc983ce2",
            "guests": 3,
            "checkInDate": "2023-03-10T16:00:00",
            "checkOutDate": "2023-03-15T13:20:00"
        }
        """;

        when(dateConfiguration.newDate()).thenReturn(new Date(1672444800000L));
        when(uuidProvider.generateUuid()).thenReturn("f2cccd2f-5711-4356-a13a-f687dc983ce5");

        // Act
        ResultActions resultActions = mockMvc.perform(post("/private/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("Authorization", "Bearer " + token));

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.message").value("Booking created successfully"));
                });

        List<Map> savedBooking = mongoTemplate.findAll(Map.class, "BOOKING");

        assertThat(savedBooking).hasSize(1);

        assertThat((Map<String, Object>) savedBooking.getFirst())
                .containsExactlyInAnyOrderEntriesOf(ofEntries(
                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce5"),
                        entry("userId", "f2cccd2f-5711-4356-a13a-f687dc983ce9"),
                        entry("status", "PENDING"),
                        entry("isPaid", false),
                        entry("totalPrice", 600.0),
                        entry("checkInDate", new Date(1678464000000L)),
                        entry("checkOutDate", new Date(1678886400000L)),
                        entry("guests", 3),
                        entry("hotelRoom", Map.ofEntries(
                                entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce2"),
                                entry("price", 120.0),
                                entry("maxOccupancy", 3),
                                entry("features", List.of("ROOM_SERVICE", "BALCONY")),
                                entry("type", "SINGLE")
                        )),
                        entry("hotel", Map.ofEntries(
                                entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce1"),
                                entry("name", "HotelParadise"),
                                entry("picture_list", List.of("https://example.com/pic1.jpg", "https://example.com/pic2.jpg")),
                                entry("amenities", List.of("PARKING", "BAR", "POOL", "GYM")),
                                entry("stars", 4),
                                entry("rooms", List.of(
                                        Map.ofEntries(
                                                entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce2"),
                                                entry("price", 120.0),
                                                entry("maxOccupancy", 3),
                                                entry("features", List.of("ROOM_SERVICE", "BALCONY")),
                                                entry("type", "SINGLE")
                                        ),
                                        Map.ofEntries(
                                                entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce3"),
                                                entry("price", 150.0),
                                                entry("maxOccupancy", 5),
                                                entry("features", List.of("WIFI", "HAIR_DRYER")),
                                                entry("type", "DOUBLE")
                                        )
                                )),
                                entry("location", Map.ofEntries(
                                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce4"),
                                        entry("address", "123 Paradise St"),
                                        entry("city", "Paradise City"),
                                        entry("state", "California"),
                                        entry("country", "USA"),
                                        entry("postalCode", "90210"),
                                        entry("googleMapsUrl", "https://maps.google.com/?q=123+Paradise+St")
                                ))
                        ))
                ));
    }

    @Test
    void shouldRetrieveBooking() throws Exception {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
            "userId": "f2cccd2f-5711-4356-a13a-f687dc983ce9",
            "status": "CONFIRMED",
            "isPaid": true,
            "totalPrice": 600.0,
            "checkInDate": { "$date": "2025-03-10T14:00:00.000Z" },
            "checkOutDate": { "$date": "2025-03-15T12:00:00.000Z" },
            "guests": 3,
            "hotelRoom": {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce2",
                "type": "SINGLE",
                "price": 120,
                "maxOccupancy": 3,
                "features": ["ROOM_SERVICE", "BALCONY"]
            },
            "hotel": {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce3",
                "name": "HotelParadise",
                "picture_list": ["https://example.com/pic1.jpg", "https://example.com/pic2.jpg"],
                "amenities": ["PARKING", "BAR", "POOL", "GYM"],
                "stars": 4,
                "rooms": [
                    {
                        "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce2",
                        "type": "SINGLE",
                        "price": 120,
                        "maxOccupancy": 3,
                        "features": ["ROOM_SERVICE", "BALCONY"]
                    },
                    {
                        "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce4",
                        "type": "DOUBLE",
                        "price": 150,
                        "maxOccupancy": 5,
                        "features": ["WIFI", "HAIR_DRYER"]
                    }
                ],
                "location": {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce5",
                    "address": "123 Paradise St",
                    "city": "Paradise City",
                    "state": "California",
                    "country": "USA",
                    "postalCode": "90210",
                    "googleMapsUrl": "https://maps.google.com/?q=123+Paradise+St"
                }
            }
        }
        """, "BOOKING");

        // Act
        ResultActions resultActions = mockMvc.perform(get("/private/booking/{bookingId}", "f2cccd2f-5711-4356-a13a-f687dc983ce1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.informations.booking.id").value("f2cccd2f-5711-4356-a13a-f687dc983ce1"))
                .andExpect(jsonPath("$.informations.booking.userId").value("f2cccd2f-5711-4356-a13a-f687dc983ce9"))
                .andExpect(jsonPath("$.informations.booking.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.informations.booking.paid").value(true))
                .andExpect(jsonPath("$.informations.booking.totalPrice").value(600.0))
                .andExpect(jsonPath("$.informations.booking.checkInDate").value(new Date(1741615200000L)))
                .andExpect(jsonPath("$.informations.booking.checkOutDate").value(new Date(1742040000000L)))
                .andExpect(jsonPath("$.informations.booking.guests").value(3))
                .andExpect(jsonPath("$.informations.booking.hotelRoom.id").value("f2cccd2f-5711-4356-a13a-f687dc983ce2"))
                .andExpect(jsonPath("$.informations.booking.hotelRoom.type").value("SINGLE"))
                .andExpect(jsonPath("$.informations.booking.hotelRoom.price").value(120))
                .andExpect(jsonPath("$.informations.booking.hotelRoom.maxOccupancy").value(3))
                .andExpect(jsonPath("$.informations.booking.hotelRoom.features[0]").value("ROOM_SERVICE"))
                .andExpect(jsonPath("$.informations.booking.hotelRoom.features[1]").value("BALCONY"))
                .andExpect(jsonPath("$.informations.booking.hotel.id").value("f2cccd2f-5711-4356-a13a-f687dc983ce3"))
                .andExpect(jsonPath("$.informations.booking.hotel.name").value("HotelParadise"))
                .andExpect(jsonPath("$.informations.booking.hotel.stars").value(4))
                .andExpect(jsonPath("$.informations.booking.hotel.picture_list[0]").value("https://example.com/pic1.jpg"))
                .andExpect(jsonPath("$.informations.booking.hotel.picture_list[1]").value("https://example.com/pic2.jpg"))
                .andExpect(jsonPath("$.informations.booking.hotel.amenities[0]").value("PARKING"))
                .andExpect(jsonPath("$.informations.booking.hotel.amenities[1]").value("BAR"))
                .andExpect(jsonPath("$.informations.booking.hotel.amenities[2]").value("POOL"))
                .andExpect(jsonPath("$.informations.booking.hotel.amenities[3]").value("GYM"))
                .andExpect(jsonPath("$.informations.booking.hotel.rooms[0].id").value("f2cccd2f-5711-4356-a13a-f687dc983ce2"))
                .andExpect(jsonPath("$.informations.booking.hotel.rooms[0].type").value("SINGLE"))
                .andExpect(jsonPath("$.informations.booking.hotel.rooms[0].price").value(120))
                .andExpect(jsonPath("$.informations.booking.hotel.rooms[0].maxOccupancy").value(3))
                .andExpect(jsonPath("$.informations.booking.hotel.rooms[0].features[0]").value("ROOM_SERVICE"))
                .andExpect(jsonPath("$.informations.booking.hotel.rooms[0].features[1]").value("BALCONY"))
                .andExpect(jsonPath("$.informations.booking.hotel.rooms[1].id").value("f2cccd2f-5711-4356-a13a-f687dc983ce4"))
                .andExpect(jsonPath("$.informations.booking.hotel.rooms[1].type").value("DOUBLE"))
                .andExpect(jsonPath("$.informations.booking.hotel.rooms[1].price").value(150))
                .andExpect(jsonPath("$.informations.booking.hotel.rooms[1].maxOccupancy").value(5))
                .andExpect(jsonPath("$.informations.booking.hotel.rooms[1].features[0]").value("WIFI"))
                .andExpect(jsonPath("$.informations.booking.hotel.rooms[1].features[1]").value("HAIR_DRYER"))
                .andExpect(jsonPath("$.informations.booking.hotel.location.address").value("123 Paradise St"))
                .andExpect(jsonPath("$.informations.booking.hotel.location.city").value("Paradise City"))
                .andExpect(jsonPath("$.informations.booking.hotel.location.state").value("California"))
                .andExpect(jsonPath("$.informations.booking.hotel.location.country").value("USA"))
                .andExpect(jsonPath("$.informations.booking.hotel.location.postalCode").value("90210"))
                .andExpect(jsonPath("$.informations.booking.hotel.location.googleMapsUrl").value("https://maps.google.com/?q=123+Paradise+St"));
    }

    @Test
    void shouldUpdateBooking() throws Exception {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "bookingId123",
            "userId": "f2cccd2f-5711-4356-a13a-f687dc983ce9",
            "status": "CONFIRMED",
            "isPaid": true,
            "totalPrice": 500.0,
            "checkInDate": { "$date": "2025-04-01T14:00:00.000Z" },
            "checkOutDate": { "$date": "2025-04-05T12:00:00.000Z" },
            "guests": 2,
            "hotelRoom": {
                "_id": "hotelRoomId123",
                "type": "SINGLE",
                "price": 100.0,
                "maxOccupancy": 3,
                "features": ["WIFI", "BALCONY"]
            },
            "hotel": {
                "_id": "hotelId123",
                "name": "Luxury Hotel",
                "picture_list": ["https://example.com/hotel1.jpg", "https://example.com/hotel2.jpg"],
                "amenities": ["SPA", "POOL", "GYM"],
                "stars": 4,
                "rooms": [
                    {
                        "_id": "hotelRoomId123",
                        "type": "SINGLE",
                        "price": 100.0,
                        "maxOccupancy": 3,
                        "features": ["WIFI", "BALCONY"]
                    },
                    {
                        "_id": "hotelRoomId456",
                        "type": "DOUBLE",
                        "price": 150.0,
                        "maxOccupancy": 4,
                        "features": ["ROOM_SERVICE", "HAIR_DRYER"]
                    }
                ],
                "location": {
                    "_id": "locationId123",
                    "address": "456 Luxury St",
                    "city": "Dream City",
                    "state": "New York",
                    "country": "USA",
                    "postalCode": "10001",
                    "googleMapsUrl": "https://maps.google.com/?q=456+Luxury+St"
                }
            }
        }
        """, "BOOKING");

        String body = """
        {
            "bookingId": "bookingId123",
            "guests": 3,
            "checkInDate": "2025-04-02T14:00:00",
            "checkOutDate": "2025-04-06T12:00:00"
        }
        """;

        when(dateConfiguration.newDate()).thenReturn(new Date(1672444800000L));

        // Act
        ResultActions resultActions = mockMvc.perform(put("/private/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("Authorization", "Bearer " + token));

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.message").value("Booking updated successfully"));
                });

        List<Map> updatedBooking = mongoTemplate.findAll(Map.class, "BOOKING");

        assertThat(updatedBooking).hasSize(1);

        assertThat((Map<String, Object>) updatedBooking.getFirst())
                .containsExactlyInAnyOrderEntriesOf(ofEntries(
                        entry("_id", "bookingId123"),
                        entry("userId", "f2cccd2f-5711-4356-a13a-f687dc983ce9"),
                        entry("status", "PENDING"),
                        entry("isPaid", false),
                        entry("totalPrice", 400.0),
                        entry("checkInDate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse("2025-04-02T16:00:00")),
                        entry("checkOutDate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse("2025-04-06T14:00:00")),
                        entry("guests", 3),
                        entry("hotelRoom", Map.ofEntries(
                                entry("_id", "hotelRoomId123"),
                                entry("type", "SINGLE"),
                                entry("price", 100.0),
                                entry("maxOccupancy", 3),
                                entry("features", List.of("WIFI", "BALCONY"))
                        )),
                        entry("hotel", Map.ofEntries(
                                entry("_id", "hotelId123"),
                                entry("name", "Luxury Hotel"),
                                entry("picture_list", List.of("https://example.com/hotel1.jpg", "https://example.com/hotel2.jpg")),
                                entry("amenities", List.of("SPA", "POOL", "GYM")),
                                entry("stars", 4),
                                entry("rooms", List.of(
                                        Map.ofEntries(
                                                entry("_id", "hotelRoomId123"),
                                                entry("type", "SINGLE"),
                                                entry("price", 100.0),
                                                entry("maxOccupancy", 3),
                                                entry("features", List.of("WIFI", "BALCONY"))
                                        ),
                                        Map.ofEntries(
                                                entry("_id", "hotelRoomId456"),
                                                entry("type", "DOUBLE"),
                                                entry("price", 150.0),
                                                entry("maxOccupancy", 4),
                                                entry("features", List.of("ROOM_SERVICE", "HAIR_DRYER"))
                                        )
                                )),
                                entry("location", Map.ofEntries(
                                        entry("_id", "locationId123"),
                                        entry("address", "456 Luxury St"),
                                        entry("city", "Dream City"),
                                        entry("state", "New York"),
                                        entry("country", "USA"),
                                        entry("postalCode", "10001"),
                                        entry("googleMapsUrl", "https://maps.google.com/?q=456+Luxury+St")
                                ))
                        ))
                ));
    }

    @Test
    void shouldDeleteBooking() throws Exception {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
            "userId": "f2cccd2f-5711-4356-a13a-f687dc983ce9",
            "status": "CONFIRMED",
            "isPaid": true,
            "totalPrice": 500.0,
            "checkInDate": { "$date": "2025-04-01T14:00:00.000Z" },
            "checkOutDate": { "$date": "2025-04-05T12:00:00.000Z" },
            "guests": 2,
            "hotelRoom": {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce2",
                "type": "SINGLE",
                "price": 100.0,
                "maxOccupancy": 3,
                "features": ["WIFI", "BALCONY"]
            },
            "hotel": {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce3",
                "name": "Luxury Hotel",
                "picture_list": ["https://example.com/hotel1.jpg", "https://example.com/hotel2.jpg"],
                "amenities": ["SPA", "POOL", "GYM"],
                "stars": 4,
                "rooms": [
                    {
                        "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce4",
                        "type": "SINGLE",
                        "price": 100.0,
                        "maxOccupancy": 3,
                        "features": ["WIFI", "BALCONY"]
                    },
                    {
                        "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce5",
                        "type": "DOUBLE",
                        "price": 150.0,
                        "maxOccupancy": 4,
                        "features": ["ROOM_SERVICE", "HAIR_DRYER"]
                    }
                ],
                "location": {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce6",
                    "address": "456 Luxury St",
                    "city": "Dream City",
                    "state": "New York",
                    "country": "USA",
                    "postalCode": "10001",
                    "googleMapsUrl": "https://maps.google.com/?q=456+Luxury+St"
                }
            }
        }
        """, "BOOKING");

        // Act
        ResultActions resultActions = mockMvc.perform(delete("/private/booking/{bookingId}", "f2cccd2f-5711-4356-a13a-f687dc983ce1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token));

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.message").value("Booking deleted successfully"));
                });

        List<Booking> remainingBookings = mongoTemplate.findAll(Booking.class, "BOOKING");
        assertThat(remainingBookings).isEmpty();
    }

    @Test
    void shouldGetUserBookings() throws Exception {
        mongoTemplate.insert("""
        {
            "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
            "userId": "f2cccd2f-5711-4356-a13a-f687dc983ce9",
            "status": "CONFIRMED",
            "isPaid": true,
            "totalPrice": 500.0,
            "checkInDate": { "$date": "2025-04-01T14:00:00.000Z" },
            "checkOutDate": { "$date": "2025-04-05T12:00:00.000Z" },
            "guests": 2,
            "hotelRoom": {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce2",
                "type": "SINGLE",
                "price": 100.0,
                "maxOccupancy": 3,
                "features": ["WIFI", "BALCONY"]
            },
            "hotel": {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce3",
                "name": "Luxury Hotel",
                "picture_list": ["https://example.com/hotel1.jpg", "https://example.com/hotel2.jpg"],
                "amenities": ["SPA", "POOL", "GYM"],
                "stars": 4,
                "location": {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce6",
                    "city": "Dream City",
                    "country": "USA"
                }
            }
        }
        """, "BOOKING");

        mongoTemplate.insert("""
        {
            "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce7",
            "userId": "f2cccd2f-5711-4356-a13a-f687dc983ce9",
            "status": "PENDING",
            "isPaid": false,
            "totalPrice": 750.0,
            "checkInDate": { "$date": "2025-05-10T14:00:00.000Z" },
            "checkOutDate": { "$date": "2025-05-15T12:00:00.000Z" },
            "guests": 3,
            "hotelRoom": {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce8",
                "type": "DOUBLE",
                "price": 150.0,
                "maxOccupancy": 4,
                "features": ["ROOM_SERVICE", "HAIR_DRYER"]
            },
            "hotel": {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce9",
                "name": "Grand Resort",
                "picture_list": ["https://example.com/resort1.jpg", "https://example.com/resort2.jpg"],
                "amenities": ["POOL", "RESTAURANT", "BAR"],
                "stars": 5,
                "location": {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce10",
                    "city": "Paradise City",
                    "country": "France"
                }
            }
        }
        """, "BOOKING");

        mongoTemplate.insert("""
        {
            "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce11",
            "userId": "different-user-id",
            "status": "CONFIRMED",
            "isPaid": true,
            "totalPrice": 300.0,
            "checkInDate": { "$date": "2025-06-01T14:00:00.000Z" },
            "checkOutDate": { "$date": "2025-06-03T12:00:00.000Z" },
            "guests": 1,
            "hotelRoom": {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce12",
                "type": "SINGLE",
                "price": 100.0,
                "maxOccupancy": 2,
                "features": ["WIFI"]
            },
            "hotel": {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce13",
                "name": "Budget Inn",
                "stars": 3,
                "location": {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce14",
                    "city": "Small Town",
                    "country": "USA"
                }
            }
        }
        """, "BOOKING");

        // Act
        ResultActions resultActions = mockMvc.perform(get("/private/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token));

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.informations.bookings").isArray())
                            .andExpect(jsonPath("$.informations.bookings.length()").value(2))
                            .andExpect(jsonPath("$.informations.bookings[0].id").value("f2cccd2f-5711-4356-a13a-f687dc983ce1"))
                            .andExpect(jsonPath("$.informations.bookings[0].userId").value("f2cccd2f-5711-4356-a13a-f687dc983ce9"))
                            .andExpect(jsonPath("$.informations.bookings[0].status").value("CONFIRMED"))
                            .andExpect(jsonPath("$.informations.bookings[0].paid").value(true))
                            .andExpect(jsonPath("$.informations.bookings[0].totalPrice").value(500.0))
                            .andExpect(jsonPath("$.informations.bookings[0].checkInDate").exists())
                            .andExpect(jsonPath("$.informations.bookings[0].checkOutDate").exists())
                            .andExpect(jsonPath("$.informations.bookings[0].guests").value(2))
                            .andExpect(jsonPath("$.informations.bookings[0].hotelRoom.id").value("f2cccd2f-5711-4356-a13a-f687dc983ce2"))
                            .andExpect(jsonPath("$.informations.bookings[0].hotelRoom.type").value("SINGLE"))
                            .andExpect(jsonPath("$.informations.bookings[0].hotelRoom.price").value(100.0))
                            .andExpect(jsonPath("$.informations.bookings[0].hotelRoom.maxOccupancy").value(3))
                            .andExpect(jsonPath("$.informations.bookings[0].hotelRoom.features").isArray())
                            .andExpect(jsonPath("$.informations.bookings[0].hotelRoom.features.length()").value(2))
                            .andExpect(jsonPath("$.informations.bookings[0].hotelRoom.features[0]").value("WIFI"))
                            .andExpect(jsonPath("$.informations.bookings[0].hotelRoom.features[1]").value("BALCONY"))
                            .andExpect(jsonPath("$.informations.bookings[0].hotel.id").value("f2cccd2f-5711-4356-a13a-f687dc983ce3"))
                            .andExpect(jsonPath("$.informations.bookings[0].hotel.name").value("Luxury Hotel"))
                            .andExpect(jsonPath("$.informations.bookings[0].hotel.picture_list").isArray())
                            .andExpect(jsonPath("$.informations.bookings[0].hotel.picture_list.length()").value(2))
                            .andExpect(jsonPath("$.informations.bookings[0].hotel.picture_list[0]").value("https://example.com/hotel1.jpg"))
                            .andExpect(jsonPath("$.informations.bookings[0].hotel.picture_list[1]").value("https://example.com/hotel2.jpg"))
                            .andExpect(jsonPath("$.informations.bookings[0].hotel.amenities").isArray())
                            .andExpect(jsonPath("$.informations.bookings[0].hotel.amenities.length()").value(3))
                            .andExpect(jsonPath("$.informations.bookings[0].hotel.amenities[0]").value("SPA"))
                            .andExpect(jsonPath("$.informations.bookings[0].hotel.amenities[1]").value("POOL"))
                            .andExpect(jsonPath("$.informations.bookings[0].hotel.amenities[2]").value("GYM"))
                            .andExpect(jsonPath("$.informations.bookings[0].hotel.stars").value(4))
                            .andExpect(jsonPath("$.informations.bookings[0].hotel.location.id").value("f2cccd2f-5711-4356-a13a-f687dc983ce6"))
                            .andExpect(jsonPath("$.informations.bookings[0].hotel.location.city").value("Dream City"))
                            .andExpect(jsonPath("$.informations.bookings[0].hotel.location.country").value("USA"))
                            .andExpect(jsonPath("$.informations.bookings[1].id").value("f2cccd2f-5711-4356-a13a-f687dc983ce7"))
                            .andExpect(jsonPath("$.informations.bookings[1].userId").value("f2cccd2f-5711-4356-a13a-f687dc983ce9"))
                            .andExpect(jsonPath("$.informations.bookings[1].status").value("PENDING"))
                            .andExpect(jsonPath("$.informations.bookings[1].paid").value(false))
                            .andExpect(jsonPath("$.informations.bookings[1].totalPrice").value(750.0))
                            .andExpect(jsonPath("$.informations.bookings[1].checkInDate").exists())
                            .andExpect(jsonPath("$.informations.bookings[1].checkOutDate").exists())
                            .andExpect(jsonPath("$.informations.bookings[1].guests").value(3))
                            .andExpect(jsonPath("$.informations.bookings[1].hotelRoom.id").value("f2cccd2f-5711-4356-a13a-f687dc983ce8"))
                            .andExpect(jsonPath("$.informations.bookings[1].hotelRoom.type").value("DOUBLE"))
                            .andExpect(jsonPath("$.informations.bookings[1].hotelRoom.price").value(150.0))
                            .andExpect(jsonPath("$.informations.bookings[1].hotelRoom.maxOccupancy").value(4))
                            .andExpect(jsonPath("$.informations.bookings[1].hotelRoom.features").isArray())
                            .andExpect(jsonPath("$.informations.bookings[1].hotelRoom.features.length()").value(2))
                            .andExpect(jsonPath("$.informations.bookings[1].hotelRoom.features[0]").value("ROOM_SERVICE"))
                            .andExpect(jsonPath("$.informations.bookings[1].hotelRoom.features[1]").value("HAIR_DRYER"))
                            .andExpect(jsonPath("$.informations.bookings[1].hotel.id").value("f2cccd2f-5711-4356-a13a-f687dc983ce9"))
                            .andExpect(jsonPath("$.informations.bookings[1].hotel.name").value("Grand Resort"))
                            .andExpect(jsonPath("$.informations.bookings[1].hotel.picture_list").isArray())
                            .andExpect(jsonPath("$.informations.bookings[1].hotel.picture_list.length()").value(2))
                            .andExpect(jsonPath("$.informations.bookings[1].hotel.picture_list[0]").value("https://example.com/resort1.jpg"))
                            .andExpect(jsonPath("$.informations.bookings[1].hotel.picture_list[1]").value("https://example.com/resort2.jpg"))
                            .andExpect(jsonPath("$.informations.bookings[1].hotel.amenities").isArray())
                            .andExpect(jsonPath("$.informations.bookings[1].hotel.amenities.length()").value(3))
                            .andExpect(jsonPath("$.informations.bookings[1].hotel.amenities[0]").value("POOL"))
                            .andExpect(jsonPath("$.informations.bookings[1].hotel.amenities[1]").value("RESTAURANT"))
                            .andExpect(jsonPath("$.informations.bookings[1].hotel.amenities[2]").value("BAR"))
                            .andExpect(jsonPath("$.informations.bookings[1].hotel.stars").value(5))
                            .andExpect(jsonPath("$.informations.bookings[1].hotel.location.id").value("f2cccd2f-5711-4356-a13a-f687dc983ce10"))
                            .andExpect(jsonPath("$.informations.bookings[1].hotel.location.city").value("Paradise City"))
                            .andExpect(jsonPath("$.informations.bookings[1].hotel.location.country").value("France"));
                });
    }

}
