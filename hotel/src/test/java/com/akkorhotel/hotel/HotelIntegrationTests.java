package com.akkorhotel.hotel;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@WireMockTest(httpPort = 8089)
public class HotelIntegrationTests {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection("HOTELS");
    }

    @Test
    void shouldReturnHotelInformations() throws Exception {
        // Arrange
        mongoTemplate.insert(""" 
            {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
                "name": "LuxuryHotel",
                "description": "A five-star experience.",
                "picture_list": ["https://picture1.jpg", "https://picture2.png"],
                "amenities": ["POOL", "WIFI"],
                "rooms": [
                    {
                        "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce2",
                        "type": "SINGLE",
                        "price": 120,
                        "maxOccupancy": 3,
                        "features": ["ROOM_SERVICE", "BALCONY"]
                    }
                ],
                "location": {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce3",
                    "address": "123 Rue de la Paix",
                    "city": "Paris",
                    "state": "Île-de-France",
                    "country": "France",
                    "postalCode": "75001",
                    "googleMapsUrl": "https://maps.google.com/?q=LuxuryHotel"
                }
            }
        """, "HOTELS");

        // Act
        ResultActions resultActions = mockMvc.perform(get("/hotel/f2cccd2f-5711-4356-a13a-f687dc983ce1")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.informations.error").doesNotExist())
                            .andExpect(jsonPath("$.informations.hotel.id").value("f2cccd2f-5711-4356-a13a-f687dc983ce1"))
                            .andExpect(jsonPath("$.informations.hotel.name").value("LuxuryHotel"))
                            .andExpect(jsonPath("$.informations.hotel.description").value("A five-star experience."))
                            .andExpect(jsonPath("$.informations.hotel.picture_list[0]").value("https://picture1.jpg"))
                            .andExpect(jsonPath("$.informations.hotel.picture_list[1]").value("https://picture2.png"))
                            .andExpect(jsonPath("$.informations.hotel.amenities[0]").value("POOL"))
                            .andExpect(jsonPath("$.informations.hotel.amenities[1]").value("WIFI"))
                            .andExpect(jsonPath("$.informations.hotel.rooms[0].id").value("f2cccd2f-5711-4356-a13a-f687dc983ce2"))
                            .andExpect(jsonPath("$.informations.hotel.rooms[0].type").value("SINGLE"))
                            .andExpect(jsonPath("$.informations.hotel.rooms[0].maxOccupancy").value(3))
                            .andExpect(jsonPath("$.informations.hotel.rooms[0].features[0]").value("ROOM_SERVICE"))
                            .andExpect(jsonPath("$.informations.hotel.rooms[0].features[1]").value("BALCONY"))
                            .andExpect(jsonPath("$.informations.hotel.rooms[0].price").value(120.00))
                            .andExpect(jsonPath("$.informations.hotel.location.id").value("f2cccd2f-5711-4356-a13a-f687dc983ce3"))
                            .andExpect(jsonPath("$.informations.hotel.location.address").value("123 Rue de la Paix"))
                            .andExpect(jsonPath("$.informations.hotel.location.city").value("Paris"))
                            .andExpect(jsonPath("$.informations.hotel.location.state").value("Île-de-France"))
                            .andExpect(jsonPath("$.informations.hotel.location.country").value("France"))
                            .andExpect(jsonPath("$.informations.hotel.location.postalCode").value("75001"))
                            .andExpect(jsonPath("$.informations.hotel.location.googleMapsUrl").value("https://maps.google.com/?q=LuxuryHotel"));
                });
    }

    @Test
    void shouldReturnHotels() throws Exception {
        // Arrange
        mongoTemplate.insert("""
            {
                "_id": "hotelId1",
                "name": "LuxuryHotel",
                "description": "A five-star experience.",
                "picture_list": ["https://mocked-image-url.com/hotel1.jpg"],
                "amenities": ["POOL", "WIFI"],
                "rooms": [],
                "location": {
                    "_id": "locationId1",
                    "address": "123 Rue de la Paix",
                    "city": "Paris",
                    "state": "Île-de-France",
                    "country": "France",
                    "postalCode": "75001",
                    "googleMapsUrl": "https://maps.google.com/?q=LuxuryHotel"
                }
            }
        """, "HOTELS");

        mongoTemplate.insert("""
            {
                "_id": "hotelId2",
                "name": "LuxurySpa",
                "description": "Relaxation at its finest.",
                "picture_list": ["https://mocked-image-url.com/spa1.jpg"],
                "amenities": ["SPA", "WIFI"],
                "rooms": [],
                "location": {
                    "_id": "locationId2",
                    "address": "456 Avenue des Champs-Élysées",
                    "city": "Paris",
                    "state": "Île-de-France",
                    "country": "France",
                    "postalCode": "75008",
                    "googleMapsUrl": "https://maps.google.com/?q=LuxurySpa"
                }
            }
        """, "HOTELS");

        mongoTemplate.insert("""
            {
                "_id": "hotelId3",
                "name": "MasterLol",
                "description": "Relaxation at its finest.",
                "picture_list": ["https://mocked-image-url.com/spa1.jpg"],
                "amenities": ["SPA", "WIFI"],
                "rooms": [],
                "location": {
                    "_id": "locationId3",
                    "address": "456 Avenue des Champs-Élysées",
                    "city": "Paris",
                    "state": "Île-de-France",
                    "country": "France",
                    "postalCode": "75008",
                    "googleMapsUrl": "https://maps.google.com/?q=LuxurySpa"
                }
            }
        """, "HOTELS");

        // Act
        ResultActions resultActions = mockMvc.perform(get("/hotel")
                .param("keyword", "Luxury")
                .param("page", "0")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.informations.hotels[0].id").value("hotelId1"))
                            .andExpect(jsonPath("$.informations.hotels[0].name").value("LuxuryHotel"))
                            .andExpect(jsonPath("$.informations.hotels[0].description").value("A five-star experience."))
                            .andExpect(jsonPath("$.informations.hotels[0].picture_list[0]").value("https://mocked-image-url.com/hotel1.jpg"))
                            .andExpect(jsonPath("$.informations.hotels[0].amenities[0]").value("POOL"))
                            .andExpect(jsonPath("$.informations.hotels[0].amenities[1]").value("WIFI"))
                            .andExpect(jsonPath("$.informations.hotels[0].location.id").value("locationId1"))
                            .andExpect(jsonPath("$.informations.hotels[0].location.address").value("123 Rue de la Paix"))
                            .andExpect(jsonPath("$.informations.hotels[0].location.city").value("Paris"))
                            .andExpect(jsonPath("$.informations.hotels[0].location.state").value("Île-de-France"))
                            .andExpect(jsonPath("$.informations.hotels[0].location.country").value("France"))
                            .andExpect(jsonPath("$.informations.hotels[0].location.postalCode").value("75001"))
                            .andExpect(jsonPath("$.informations.hotels[0].location.googleMapsUrl").value("https://maps.google.com/?q=LuxuryHotel"))
                            .andExpect(jsonPath("$.informations.hotels[1].id").value("hotelId2"))
                            .andExpect(jsonPath("$.informations.hotels[1].name").value("LuxurySpa"))
                            .andExpect(jsonPath("$.informations.hotels[1].description").value("Relaxation at its finest."))
                            .andExpect(jsonPath("$.informations.hotels[1].picture_list[0]").value("https://mocked-image-url.com/spa1.jpg"))
                            .andExpect(jsonPath("$.informations.hotels[1].amenities[0]").value("SPA"))
                            .andExpect(jsonPath("$.informations.hotels[1].amenities[1]").value("WIFI"))
                            .andExpect(jsonPath("$.informations.hotels[1].location.id").value("locationId2"))
                            .andExpect(jsonPath("$.informations.hotels[1].location.address").value("456 Avenue des Champs-Élysées"))
                            .andExpect(jsonPath("$.informations.hotels[1].location.city").value("Paris"))
                            .andExpect(jsonPath("$.informations.hotels[1].location.state").value("Île-de-France"))
                            .andExpect(jsonPath("$.informations.hotels[1].location.country").value("France"))
                            .andExpect(jsonPath("$.informations.hotels[1].location.postalCode").value("75008"))
                            .andExpect(jsonPath("$.informations.hotels[1].location.googleMapsUrl").value("https://maps.google.com/?q=LuxurySpa"))
                            .andExpect(jsonPath("$.informations.totalPages").value(1))
                            .andExpect(jsonPath("$.informations.error").doesNotExist());
                });
    }

}
