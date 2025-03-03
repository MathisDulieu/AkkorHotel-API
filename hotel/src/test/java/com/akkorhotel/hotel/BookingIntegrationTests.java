package com.akkorhotel.hotel;

import com.akkorhotel.hotel.configuration.DateConfiguration;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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


}
