package com.akkorhotel.hotel;

import com.akkorhotel.hotel.service.JwtTokenService;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection("USERS");
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        // Arrange
        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce9",
                    "username": "adminUsername",
                    "password": "adminPassword",
                    "email": "adminEmail",
                    "isValidEmail": true,
                    "role": "ADMIN"
                }
                """, "USERS");

        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
                    "username": "Cobol4Life1",
                    "password": "encodedPassword1",
                    "email": "alex.dupont42@email.com",
                    "isValidEmail": true,
                    "role": "USER"
                }
                """, "USERS");

        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce2",
                    "username": "SarahLef99",
                    "password": "encodedPassword2",
                    "email": "sarah.lefevre99@email.com",
                    "isValidEmail": true,
                    "role": "USER"
                }
                """, "USERS");

        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce3",
                    "username": "Cobol4Life2",
                    "password": "encodedPassword3",
                    "email": "karim.benali75@email.com",
                    "isValidEmail": true,
                    "role": "USER"
                }
                """, "USERS");

        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce4",
                    "username": "Cobol4Jeanne",
                    "password": "encodedPassword4",
                    "email": "jeanne.morel@email.com",
                    "isValidEmail": true,
                    "role": "USER"
                }
                """, "USERS");

        String token = Jwts.builder()
                .setSubject("f2cccd2f-5711-4356-a13a-f687dc983ce9")
                .claim("type", "access")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(172_800_000)))
                .signWith(JwtTokenService.SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();

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
                            .andExpect(jsonPath("$.users.users[1].id").value("f2cccd2f-5711-4356-a13a-f687dc983ce1"))
                            .andExpect(jsonPath("$.users.users[1].username").value("Cobol4Life1"))
                            .andExpect(jsonPath("$.users.users[1].email").value("alex.dupont42@email.com"))
                            .andExpect(jsonPath("$.users.totalPages").value(2))
                            .andExpect(jsonPath("$.users.error").doesNotExist());
                });
    }

    @Test
    void shouldGetUserById() throws Exception {
        // Arrange
        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce9",
                    "username": "adminUsername",
                    "password": "adminPassword",
                    "email": "admin.email@gmail.com",
                    "isValidEmail": true,
                    "role": "ADMIN"
                }
                """, "USERS");

        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
                    "username": "Cobol4Life",
                    "password": "encodedPassword",
                    "email": "alex.dupont42@email.com",
                    "isValidEmail": true,
                    "role": "USER"
                }
                """, "USERS");

        String token = Jwts.builder()
                .setSubject("f2cccd2f-5711-4356-a13a-f687dc983ce9")
                .claim("type", "access")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(172_800_000)))
                .signWith(JwtTokenService.SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();

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
                            .andExpect(jsonPath("$.user.user.password").doesNotExist())
                            .andExpect(jsonPath("$.user.error").doesNotExist());
                });
    }

    @Test
    void shouldUpdateUser() throws Exception {
        // Arrange
        mongoTemplate.insert("""
            {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce9",
                "username": "adminUsername",
                "password": "adminPassword",
                "email": "adminEmail",
                "isValidEmail": true,
                "role": "ADMIN"
            }
            """, "USERS");

        mongoTemplate.insert("""
            {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
                "username": "username1",
                "password": "encodedPassword1",
                "email": "email1",
                "isValidEmail": true,
                "role": "USER"
            }
            """, "USERS");

        mongoTemplate.insert("""
            {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce2",
                "username": "username2",
                "password": "encodedPassword2",
                "email": "email2",
                "isValidEmail": true,
                "role": "USER"
            }
            """, "USERS");

        String userId = "f2cccd2f-5711-4356-a13a-f687dc983ce2";

        String body = """
        {
            "username": "newUsername",
            "email": "new.email@example.com",
            "role": "ADMIN",
            "isValidEmail": false
        }
        """;

        String token = Jwts.builder()
                .setSubject("f2cccd2f-5711-4356-a13a-f687dc983ce9")
                .claim("type", "access")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(172_800_000)))
                .signWith(JwtTokenService.SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();

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
                        entry("role", "ADMIN")
                ));

        assertThat((Map<String, Object>) savedUsers.get(1))
                .containsAllEntriesOf(ofEntries(
                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce1"),
                        entry("username", "username1"),
                        entry("email", "email1"),
                        entry("password", "encodedPassword1"),
                        entry("isValidEmail", true),
                        entry("role", "USER")
                ));

        assertThat((Map<String, Object>) savedUsers.getLast())
                .containsAllEntriesOf(ofEntries(
                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce2"),
                        entry("username", "newUsername"),
                        entry("email", "new.email@example.com"),
                        entry("password", "encodedPassword2"),
                        entry("isValidEmail", false),
                        entry("role", "ADMIN")
                ));
    }

}
