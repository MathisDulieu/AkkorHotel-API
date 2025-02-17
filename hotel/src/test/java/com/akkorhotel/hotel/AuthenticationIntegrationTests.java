package com.akkorhotel.hotel;

import com.akkorhotel.hotel.service.EmailService;
import com.akkorhotel.hotel.service.JwtTokenService;
import com.akkorhotel.hotel.service.UuidProvider;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
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

import static com.akkorhotel.hotel.service.JwtTokenService.SECRET_KEY;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@WireMockTest(httpPort = 8089)
public class AuthenticationIntegrationTests {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UuidProvider uuidProvider;

    @MockBean
    private EmailService emailService;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;


    @AfterEach
    void clean() {
        mongoTemplate.dropCollection("USERS");
    }

    @Test
    void shouldRegisterNewUser() throws Exception {
        // Arrange
        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
                    "username": "username1",
                    "password": "password1",
                    "email": "email1",
                    "isValidEmail": false,
                    "role": "USER"
                }
                """, "USERS");

        String body = """
                {
                    "username": "alice123",
                    "email": "alice@example.com",
                    "password": "AliceStrongP@ss1!"
                }
                """;

        when(uuidProvider.generateUuid()).thenReturn("f2cccd2f-5711-4356-a13a-f687dc983ce2");
        when(passwordEncoder.encode("AliceStrongP@ss1!")).thenReturn("encodedPassword");

        // Act
        ResultActions resultActions = mockMvc.perform(post("/auth/register")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.message").value("User successfully registered!"));
                });


        List<Map> savedUsers = mongoTemplate.findAll(Map.class, "USERS");

        assertThat(savedUsers).hasSize(2);

        assertThat((Map<String, Object>) savedUsers.getFirst())
                .containsAllEntriesOf(ofEntries(
                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce1"),
                        entry("username", "username1"),
                        entry("email", "email1"),
                        entry("password", "password1"),
                        entry("isValidEmail", false),
                        entry("role", "USER")
                ));

        assertThat((Map<String, Object>) savedUsers.getLast())
                .containsAllEntriesOf(ofEntries(
                        entry("_id","f2cccd2f-5711-4356-a13a-f687dc983ce2"),
                        entry("username", "alice123"),
                        entry("email", "alice@example.com"),
                        entry("password", "encodedPassword"),
                        entry("isValidEmail", false),
                        entry("role", "USER")
                ));

        verify(emailService, times(1)).sendEmail(eq("alice@example.com"), anyString(), anyString());
    }

    @Test
    void shouldLoginUser() throws Exception {
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

        String body = """
                {
                    "email": "alice@example.com",
                    "password": "AliceStrongP@ss1!"
                }
                """;

        when(passwordEncoder.matches("AliceStrongP@ss1!", "encodedPassword")).thenReturn(true);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/auth/login")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.token").isNotEmpty());
                });

        String jsonResponse = resultActions.andReturn().getResponse().getContentAsString();
        String token = JsonPath.parse(jsonResponse).read("$.token");

        assertThat(token).isNotBlank();

        Jws<Claims> parsedToken = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token);

        Claims claims = parsedToken.getBody();
        assertThat(claims.getSubject()).isEqualTo("f2cccd2f-5711-4356-a13a-f687dc983ce1");
        assertThat(claims.get("type", String.class)).isEqualTo("access");
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    void shouldConfirmUserEmail() throws Exception {
        // Arrange
        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
                    "username": "username",
                    "password": "encodedPassword",
                    "email": "alice@example.com",
                    "isValidEmail": false,
                    "role": "USER"
                }
                """, "USERS");

        String token = Jwts.builder()
                .setSubject("f2cccd2f-5711-4356-a13a-f687dc983ce1")
                .claim("type", "email_confirmation")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(172_800_000)))
                .signWith(JwtTokenService.SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();

        String body = """
            {
                "token": "%s"
            }
            """.formatted(token);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/auth/confirm-email")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.message").value("Email successfully validated"));
                });

        List<Map> savedUsers = mongoTemplate.findAll(Map.class, "USERS");

        assertThat(savedUsers).hasSize(1);

        assertThat((Map<String, Object>) savedUsers.getFirst())
                .containsAllEntriesOf(ofEntries(
                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce1"),
                        entry("username", "username"),
                        entry("email", "alice@example.com"),
                        entry("password", "encodedPassword"),
                        entry("isValidEmail", true),
                        entry("role", "USER")
                ));
    }

    @Test
    void shouldSendRegisterConfirmationEmail() throws Exception {
        // Arrange
        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
                    "username": "username",
                    "password": "encodedPassword",
                    "email": "alice@example.com",
                    "isValidEmail": false,
                    "role": "USER"
                }
                """, "USERS");

        String body = """
            {
                "email": "alice@example.com"
            }
            """;

        // Act
        ResultActions resultActions = mockMvc.perform(post("/auth/resend-confirmation-email")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // Assert
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    resultActions.andExpect(status().isOk())
                            .andExpect(jsonPath("$.message").value("Confirmation email successfully sent"));
                });

        verify(emailService, times(1)).sendEmail(eq("alice@example.com"), anyString(), anyString());
    }

}
