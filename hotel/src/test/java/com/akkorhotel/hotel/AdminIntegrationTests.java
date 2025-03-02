package com.akkorhotel.hotel;

import com.akkorhotel.hotel.service.JwtTokenService;
import com.akkorhotel.hotel.service.UuidProvider;
import com.akkorhotel.hotel.utils.ImageUtils;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doReturn;
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
public class AdminIntegrationTests {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageUtils imageUtils;

    @MockBean
    private UuidProvider uuidProvider;

    private String token;

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection("USERS");
        mongoTemplate.dropCollection("HOTELS");
        mongoTemplate.dropCollection("IMAGES");
        mongoTemplate.dropCollection("HOTEL_ROOMS");
    }

    @BeforeEach
    void setUp() {
        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce9",
                    "username": "adminUsername",
                    "password": "adminPassword",
                    "email": "adminEmail",
                    "isValidEmail": true,
                    "role": "ADMIN",
                    "profileImageUrl": "https://admin-profile-image.jpg"
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

    @Test
    void shouldGetAllUsers() throws Exception {
        // Arrange
        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
                    "username": "Cobol4Life1",
                    "password": "encodedPassword1",
                    "email": "alex.dupont42@email.com",
                    "isValidEmail": true,
                    "role": "USER",
                    "profileImageUrl": "https://profile-image1.jpg"
                }
                """, "USERS");

        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce2",
                    "username": "SarahLef99",
                    "password": "encodedPassword2",
                    "email": "sarah.lefevre99@email.com",
                    "isValidEmail": true,
                    "role": "USER",
                    "profileImageUrl": "https://profile-image2.jpg"
                }
                """, "USERS");

        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce3",
                    "username": "Cobol4Life2",
                    "password": "encodedPassword3",
                    "email": "karim.benali75@email.com",
                    "isValidEmail": true,
                    "role": "USER",
                    "profileImageUrl": "https://profile-image3.jpg"
                }
                """, "USERS");

        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce4",
                    "username": "Cobol4Jeanne",
                    "password": "encodedPassword4",
                    "email": "jeanne.morel@email.com",
                    "isValidEmail": true,
                    "role": "USER",
                    "profileImageUrl": "https://profile-image4.jpg"
                }
                """, "USERS");

        // Act
        ResultActions resultActions = mockMvc.perform(get("/private/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .param("keyword", "Cobol4")
                .param("page", "0")
                .param("pageSize", "2")
                .header("Authorization", "Bearer " + token)
        );

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.users.users").isArray())
                            .andExpect(jsonPath("$.users.users.length()").value(2))
                            .andExpect(jsonPath("$.users.users[0].id").value("f2cccd2f-5711-4356-a13a-f687dc983ce4"))
                            .andExpect(jsonPath("$.users.users[0].username").value("Cobol4Jeanne"))
                            .andExpect(jsonPath("$.users.users[0].email").value("jeanne.morel@email.com"))
                            .andExpect(jsonPath("$.users.users[0].profileImageUrl").value("https://profile-image4.jpg"))
                            .andExpect(jsonPath("$.users.users[1].id").value("f2cccd2f-5711-4356-a13a-f687dc983ce1"))
                            .andExpect(jsonPath("$.users.users[1].username").value("Cobol4Life1"))
                            .andExpect(jsonPath("$.users.users[1].email").value("alex.dupont42@email.com"))
                            .andExpect(jsonPath("$.users.users[1].profileImageUrl").value("https://profile-image1.jpg"))
                            .andExpect(jsonPath("$.users.totalPages").value(2))
                            .andExpect(jsonPath("$.users.error").doesNotExist());
                });
    }

    @Test
    void shouldGetUserById() throws Exception {
        // Arrange
        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
                    "username": "Cobol4Life",
                    "password": "encodedPassword",
                    "email": "alex.dupont42@email.com",
                    "isValidEmail": true,
                    "role": "USER",
                    "profileImageUrl": "https://profile-image.jpg"
                }
                """, "USERS");

        // Act
        ResultActions resultActions = mockMvc.perform(get("/private/admin/user/{userId}", "f2cccd2f-5711-4356-a13a-f687dc983ce1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
        );

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.user.user.id").value("f2cccd2f-5711-4356-a13a-f687dc983ce1"))
                            .andExpect(jsonPath("$.user.user.username").value("Cobol4Life"))
                            .andExpect(jsonPath("$.user.user.email").value("alex.dupont42@email.com"))
                            .andExpect(jsonPath("$.user.user.isValidEmail").value(true))
                            .andExpect(jsonPath("$.user.user.role").value("USER"))
                            .andExpect(jsonPath("$.user.user.profileImageUrl").value("https://profile-image.jpg"))
                            .andExpect(jsonPath("$.user.user.password").doesNotExist())
                            .andExpect(jsonPath("$.user.error").doesNotExist());
                });
    }

    @Test
    void shouldUpdateUser() throws Exception {
        // Arrange
        mongoTemplate.insert("""
            {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
                "username": "username1",
                "password": "encodedPassword1",
                "email": "email1",
                "isValidEmail": true,
                "role": "USER",
                "profileImageUrl": "https://profile-image1.jpg"
            }
            """, "USERS");

        mongoTemplate.insert("""
            {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce2",
                "username": "username2",
                "password": "encodedPassword2",
                "email": "email2",
                "isValidEmail": true,
                "role": "USER",
                "profileImageUrl": "https://profile-image2.jpg"
            }
            """, "USERS");

        String userId = "f2cccd2f-5711-4356-a13a-f687dc983ce2";

        String body = """
        {
            "username": "newUsername",
            "email": "new.email@example.com",
            "role": "ADMIN",
            "isValidEmail": false,
            "profileImageUrl": "https://new-profile-image.png"
        }
        """;

        // Act
        ResultActions resultActions = mockMvc.perform(put("/private/admin/user/{userId}", userId)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
        );

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.message").value("User with id: f2cccd2f-5711-4356-a13a-f687dc983ce2 updated successfully"));
                });

        List<Map> savedUsers = mongoTemplate.findAll(Map.class, "USERS");

        assertThat(savedUsers).hasSize(3);

        assertThat((Map<String, Object>) savedUsers.getFirst())
                .containsAllEntriesOf(ofEntries(
                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce9"),
                        entry("username", "adminUsername"),
                        entry("email", "adminEmail"),
                        entry("password", "adminPassword"),
                        entry("isValidEmail", true),
                        entry("role", "ADMIN"),
                        entry("profileImageUrl", "https://admin-profile-image.jpg")
                ));

        assertThat((Map<String, Object>) savedUsers.get(1))
                .containsAllEntriesOf(ofEntries(
                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce1"),
                        entry("username", "username1"),
                        entry("email", "email1"),
                        entry("password", "encodedPassword1"),
                        entry("isValidEmail", true),
                        entry("role", "USER"),
                        entry("profileImageUrl", "https://profile-image1.jpg")
                ));

        assertThat((Map<String, Object>) savedUsers.getLast())
                .containsAllEntriesOf(ofEntries(
                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce2"),
                        entry("username", "newUsername"),
                        entry("email", "new.email@example.com"),
                        entry("password", "encodedPassword2"),
                        entry("isValidEmail", false),
                        entry("role", "ADMIN"),
                        entry("profileImageUrl", "https://new-profile-image.png")
                ));
    }

    @Test
    void shouldCreateHotel() throws Exception {
        // Arrange
        MockMultipartFile mockFile1 = new MockMultipartFile("pictures", "hotel-image1.png", MediaType.IMAGE_PNG_VALUE, "image-content-1".getBytes());
        MockMultipartFile mockFile2 = new MockMultipartFile("pictures", "hotel-image2.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content-2".getBytes());

        String body = """
            {
                "name": "LuxuryHotel",
                "description": "A five-star experience.",
                "city": "Paris",
                "address": "123 Rue de la Paix",
                "country": "France",
                "googleMapsUrl": "https://maps.google.com/?q=LuxuryHotel",
                "state": "Île-de-France",
                "postalCode": "75001",
                "amenities": ["POOL", "WIFI"]
            }
            """;

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                body.getBytes(StandardCharsets.UTF_8)
        );

        doReturn("https://mocked-image-url.com/hotel1.jpg").when(imageUtils).uploadImage(mockFile1);
        doReturn("https://mocked-image-url.com/hotel2.jpg").when(imageUtils).uploadImage(mockFile2);

        // Act
        ResultActions resultActions = mockMvc.perform(multipart("/private/admin/hotel")
                .file(requestPart)
                .file(mockFile1)
                .file(mockFile2)
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.message").value("Hotel created successfully"));
                });

        List<Map> savedHotels = mongoTemplate.findAll(Map.class, "HOTELS");

        assertThat(savedHotels).hasSize(1);
        assertThat((Map<String, Object>) savedHotels.getFirst())
                .containsAllEntriesOf(ofEntries(
                        entry("name", "LuxuryHotel"),
                        entry("description", "A five-star experience."),
                        entry("amenities", List.of("POOL", "WIFI")),
                        entry("picture_list", List.of("https://mocked-image-url.com/hotel1.jpg", "https://mocked-image-url.com/hotel2.jpg")),
                        entry("location", Map.ofEntries(
                                entry("address", "123 Rue de la Paix"),
                                entry("city", "Paris"),
                                entry("country", "France"),
                                entry("googleMapsUrl", "https://maps.google.com/?q=LuxuryHotel"),
                                entry("postalCode", "75001"),
                                entry("state", "Île-de-France")
                        ))
                ));
    }

    @Test
    void shouldAddNewRoomToHotel() throws Exception {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
            "name": "LuxuryHotel",
            "description": "A five-star experience.",
            "picture_list": ["https://mocked-image-url.com/hotel1.jpg", "https://mocked-image-url.com/hotel2.jpg"],
            "amenities": ["POOL", "WIFI"],
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
                "address": "123 Rue de la Paix",
                "city": "Paris",
                "state": "Île-de-France",
                "country": "France",
                "postalCode": "75001",
                "googleMapsUrl": "https://maps.google.com/?q=LuxuryHotel"
            }
        }
        """, "HOTELS");

        String requestBody = """
        {
            "hotelId": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
            "type": "DELUXE",
            "features": ["WIFI", "MINI_FRIDGE"],
            "maxOccupancy": 3,
            "price": 250.00
        }
        """;

        when(uuidProvider.generateUuid()).thenReturn("f2cccd2f-5711-4356-a13a-f687dc983ce5");

        // Act
        ResultActions resultActions = mockMvc.perform(post("/private/admin/hotel/room")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
        );

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.message").value("HotelRoom added successfully"));
                });

        List<Map> savedHotels = mongoTemplate.findAll(Map.class, "HOTELS");

        assertThat(savedHotels).hasSize(1);
        assertThat((Map<String, Object>) savedHotels.getFirst())
                .containsAllEntriesOf(ofEntries(
                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce1"),
                        entry("name", "LuxuryHotel"),
                        entry("description", "A five-star experience."),
                        entry("amenities", List.of("POOL", "WIFI")),
                        entry("picture_list", List.of("https://mocked-image-url.com/hotel1.jpg", "https://mocked-image-url.com/hotel2.jpg")),
                        entry("location", Map.ofEntries(
                                entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce4"),
                                entry("address", "123 Rue de la Paix"),
                                entry("city", "Paris"),
                                entry("country", "France"),
                                entry("googleMapsUrl", "https://maps.google.com/?q=LuxuryHotel"),
                                entry("postalCode", "75001"),
                                entry("state", "Île-de-France")
                        )),
                        entry("rooms", List.of(
                                Map.ofEntries(
                                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce2"),
                                        entry("type", "SINGLE"),
                                        entry("price", 120.00),
                                        entry("maxOccupancy", 3),
                                        entry("features", List.of("ROOM_SERVICE", "BALCONY"))
                                ),
                                Map.ofEntries(
                                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce3"),
                                        entry("type", "DOUBLE"),
                                        entry("price", 150.00),
                                        entry("maxOccupancy", 5),
                                        entry("features", List.of("WIFI", "HAIR_DRYER"))
                                ),
                                Map.ofEntries(
                                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce5"),
                                        entry("type", "DELUXE"),
                                        entry("price", 250.00),
                                        entry("maxOccupancy", 3),
                                        entry("features", List.of("WIFI", "MINI_FRIDGE"))
                                )
                        ))
                ));

        List<Map> savedHotelRooms = mongoTemplate.findAll(Map.class, "HOTEL_ROOMS");

        assertThat(savedHotelRooms).hasSize(1);
        assertThat((Map<String, Object>) savedHotelRooms.getFirst())
                .containsAllEntriesOf(ofEntries(
                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce5"),
                        entry("type", "DELUXE"),
                        entry("price", 250.00),
                        entry("maxOccupancy", 3),
                        entry("features", List.of("WIFI", "MINI_FRIDGE"))
                ));
    }

    @Test
    void shouldDeleteRoomFromHotel() throws Exception {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
            "name": "LuxuryHotel",
            "description": "A five-star experience.",
            "picture_list": ["https://mocked-image-url.com/hotel1.jpg", "https://mocked-image-url.com/hotel2.jpg"],
            "amenities": ["POOL", "WIFI"],
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
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce2",
                    "type": "SINGLE",
                    "price": 120,
                    "maxOccupancy": 3,
                    "features": ["ROOM_SERVICE", "BALCONY"]
                }
                """, "HOTEL_ROOMS");

        String requestBody = """
        {
            "hotelId": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
            "hotelRoomId": "f2cccd2f-5711-4356-a13a-f687dc983ce2"
        }
        """;


        // Act
        ResultActions resultActions = mockMvc.perform(delete("/private/admin/hotel/room")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
        );

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.message").value("HotelRoom removed successfully"));
                });

        List<Map> savedHotels = mongoTemplate.findAll(Map.class, "HOTELS");

        assertThat(savedHotels).hasSize(1);
        assertThat((Map<String, Object>) savedHotels.getFirst())
                .containsAllEntriesOf(ofEntries(
                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce1"),
                        entry("name", "LuxuryHotel"),
                        entry("description", "A five-star experience."),
                        entry("amenities", List.of("POOL", "WIFI")),
                        entry("picture_list", List.of("https://mocked-image-url.com/hotel1.jpg", "https://mocked-image-url.com/hotel2.jpg")),
                        entry("location", Map.ofEntries(
                                entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce4"),
                                entry("address", "123 Rue de la Paix"),
                                entry("city", "Paris"),
                                entry("country", "France"),
                                entry("googleMapsUrl", "https://maps.google.com/?q=LuxuryHotel"),
                                entry("postalCode", "75001"),
                                entry("state", "Île-de-France")
                        )),
                        entry("rooms", List.of(
                                Map.ofEntries(
                                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce3"),
                                        entry("type", "DOUBLE"),
                                        entry("price", 150.00),
                                        entry("maxOccupancy", 5),
                                        entry("features", List.of("WIFI", "HAIR_DRYER"))
                                )
                        ))
                ));

        List<Map> savedHotelRooms = mongoTemplate.findAll(Map.class, "HOTEL_ROOMS");
        assertThat(savedHotelRooms).isEmpty();
    }

    @Test
    void shouldDeleteHotel() throws Exception {
        // Arrange
        mongoTemplate.insert("""
            {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
                "name": "LuxuryHotel",
                "description": "A five-star experience.",
                "picture_list": ["https://mocked-image-url.com/hotel1.jpg", "https://mocked-image-url.com/hotel2.jpg"],
                "amenities": ["POOL", "WIFI"],
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

        String hotelId = "f2cccd2f-5711-4356-a13a-f687dc983ce1";

        // Act
        ResultActions resultActions = mockMvc.perform(delete("/private/admin/hotel/{hotelId}", hotelId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
        );

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.message").value("Hotel deleted successfully"));
                });

        List<Map> savedHotels = mongoTemplate.findAll(Map.class, "HOTELS");
        assertThat(savedHotels).isEmpty();

        List<Map> savedHotelRooms = mongoTemplate.findAll(Map.class, "HOTEL_ROOMS");
        assertThat(savedHotelRooms).isEmpty();
    }

    @Test
    void shouldAddHotelPicture() throws Exception {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
            "name": "LuxuryHotel",
            "description": "A five-star experience.",
            "picture_list": ["https://mocked-image-url.com/hotel1.jpg", "https://mocked-image-url.com/hotel2.jpg"],
            "amenities": ["POOL", "WIFI"],
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
                "address": "123 Rue de la Paix",
                "city": "Paris",
                "state": "Île-de-France",
                "country": "France",
                "postalCode": "75001",
                "googleMapsUrl": "https://maps.google.com/?q=LuxuryHotel"
            }
        }
        """, "HOTELS");

        String hotelId = "f2cccd2f-5711-4356-a13a-f687dc983ce1";

        MockMultipartFile mockFile = new MockMultipartFile("picture", "picture.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes());

        doReturn("https://mocked-image-url.com/hotel3.jpg").when(imageUtils).uploadImage(mockFile);

        // Act
        ResultActions resultActions = mockMvc.perform(multipart("/private/admin/hotel/{hotelId}/picture", hotelId)
                .file(mockFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", "Bearer " + token)
        );

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.message").value("Picture added successfully"));
                });

        List<Map> savedHotels = mongoTemplate.findAll(Map.class, "HOTELS");

        assertThat(savedHotels).hasSize(1);
        assertThat((Map<String, Object>) savedHotels.getFirst())
                .containsAllEntriesOf(ofEntries(
                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce1"),
                        entry("name", "LuxuryHotel"),
                        entry("description", "A five-star experience."),
                        entry("amenities", List.of("POOL", "WIFI")),
                        entry("picture_list", List.of("https://mocked-image-url.com/hotel1.jpg", "https://mocked-image-url.com/hotel2.jpg", "https://mocked-image-url.com/hotel3.jpg")),
                        entry("location", Map.ofEntries(
                                entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce4"),
                                entry("address", "123 Rue de la Paix"),
                                entry("city", "Paris"),
                                entry("country", "France"),
                                entry("googleMapsUrl", "https://maps.google.com/?q=LuxuryHotel"),
                                entry("postalCode", "75001"),
                                entry("state", "Île-de-France")
                        )),
                        entry("rooms", List.of(
                                Map.ofEntries(
                                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce2"),
                                        entry("type", "SINGLE"),
                                        entry("price", 120.00),
                                        entry("maxOccupancy", 3),
                                        entry("features", List.of("ROOM_SERVICE", "BALCONY"))
                                ),
                                Map.ofEntries(
                                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce3"),
                                        entry("type", "DOUBLE"),
                                        entry("price", 150.00),
                                        entry("maxOccupancy", 5),
                                        entry("features", List.of("WIFI", "HAIR_DRYER"))
                                )
                        ))
                ));
    }


}
