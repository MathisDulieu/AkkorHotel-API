package com.akkorhotel.hotel;

import com.akkorhotel.hotel.service.EmailService;
import com.akkorhotel.hotel.service.JwtTokenService;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@WireMockTest(httpPort = 8089)
public class UserIntegrationTests extends AbstractContainerBaseTest{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection("USERS");
    }

    @Test
    void shouldReturnAuthenticatedUserInformations() throws Exception {
        // Arrange
        mongoTemplate.insert("""
            {
                "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
                "username": "username",
                "password": "encodedPassword",
                "email": "alice@example.com",
                "isValidEmail": true,
                "role": "USER"
            }
            """, "USERS");

        String token = Jwts.builder()
                .setSubject("f2cccd2f-5711-4356-a13a-f687dc983ce1")
                .claim("type", "access")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(172_800_000)))
                .signWith(JwtTokenService.SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();

        // Act
        ResultActions resultActions = mockMvc.perform(get("/private/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
        );

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.informations.username").value("username"))
                            .andExpect(jsonPath("$.informations.email").value("alice@example.com"))
                            .andExpect(jsonPath("$.informations.userRole").value("USER"));
                });
    }

    @Test
    void shouldUpdateExistingUser() throws Exception {
        // Arrange
        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
                    "username": "username1",
                    "password": "encodedPassword1",
                    "email": "email1",
                    "isValidEmail": false,
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

        String body = """
                {
                    "username": "alice123",
                    "email": "alice@example.com",
                    "oldPassword": "oldPasswordValue",
                    "newPassword": "AliceStrongP@ss1!"
                }
                """;

        String token = Jwts.builder()
                .setSubject("f2cccd2f-5711-4356-a13a-f687dc983ce2")
                .claim("type", "access")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(172_800_000)))
                .signWith(JwtTokenService.SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();

        when(passwordEncoder.encode("AliceStrongP@ss1!")).thenReturn("newEncodedPassword");
        when(passwordEncoder.matches("oldPasswordValue", "encodedPassword2")).thenReturn(true);

        // Act
        ResultActions resultActions = mockMvc.perform(patch("/private/user")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
        );

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.message").value("User details updated successfully"));
                });

        List<Map> savedUsers = mongoTemplate.findAll(Map.class, "USERS");

        assertThat(savedUsers).hasSize(2);

        assertThat((Map<String, Object>) savedUsers.getFirst())
                .containsAllEntriesOf(ofEntries(
                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce1"),
                        entry("username", "username1"),
                        entry("email", "email1"),
                        entry("password", "encodedPassword1"),
                        entry("isValidEmail", false),
                        entry("role", "USER")
                ));

        assertThat((Map<String, Object>) savedUsers.getLast())
                .containsAllEntriesOf(ofEntries(
                        entry("_id","f2cccd2f-5711-4356-a13a-f687dc983ce2"),
                        entry("username", "alice123"),
                        entry("email", "alice@example.com"),
                        entry("password", "newEncodedPassword"),
                        entry("isValidEmail", false),
                        entry("role", "USER")
                ));

        verify(emailService, times(1)).sendEmail(eq("alice@example.com"), anyString(), anyString());
    }

}
