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
public class UserIntegrationTests extends AbstractContainerBaseTest{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MockMvc mockMvc;

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


}
