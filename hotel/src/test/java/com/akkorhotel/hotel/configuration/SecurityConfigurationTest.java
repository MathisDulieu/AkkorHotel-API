package com.akkorhotel.hotel.configuration;

import com.akkorhotel.hotel.AbstractContainerBaseTest;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@WireMockTest(httpPort = 8089)
class SecurityConfigurationTest extends AbstractContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection("USERS");
    }

    @Test
    void shouldAllowAccessToPublicRoute() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/public/any"));

        // Assert
        int statusCode = result.andReturn().getResponse().getStatus();

        assertThat(statusCode).isNotEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(statusCode).isNotEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void shouldAllowAccessToProtectedRoute_whenUserIsAuthenticated() throws Exception {
        // Arrange
        mongoTemplate.insert("""
                {
                    "_id": "userId",
                    "role": "USER"
                }
                """, "USERS");

        String userId = "userId";

        String userToken = Jwts.builder()
                .setSubject(userId)
                .claim("type", "access")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(172_800_000)))
                .signWith(JwtTokenService.SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();

        // Act
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/private/protected/any")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken));

        // Assert
        int statusCode = result.andReturn().getResponse().getStatus();

        assertThat(statusCode).isNotEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(statusCode).isNotEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void shouldAllowAccessToAdminRoute_whenUserIsAuthenticatedWithAdminRole() throws Exception {
        // Arrange
        mongoTemplate.insert("""
                {
                    "_id": "userId",
                    "role": "ADMIN"
                }
                """, "USERS");

        String userId = "userId";

        String userToken = Jwts.builder()
                .setSubject(userId)
                .claim("type", "access")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(172_800_000)))
                .signWith(JwtTokenService.SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();

        // Act
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/private/admin/any")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken));

        // Assert
        int statusCode = result.andReturn().getResponse().getStatus();

        assertThat(statusCode).isNotEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(statusCode).isNotEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void shouldReturnUnauthorized_whenUserTriesToAccessPrivateRouteWithoutValidToken() throws Exception {
        // Arrange
        String userToken = "invalid-token";

        // Act
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/private/any")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken));

        // Assert
        int statusCode = result.andReturn().getResponse().getStatus();

        assertThat(statusCode).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldReturnUnauthorized_whenUserTriesToAccessPrivateRouteAndTokenIsNull() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/private/any")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + null));

        // Assert
        int statusCode = result.andReturn().getResponse().getStatus();

        assertThat(statusCode).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldReturnForbidden_whenUserTriesToAccessAdminRouteWithoutValidUserRole() throws Exception {
        // Arrange
        mongoTemplate.insert("""
                {
                    "_id": "userId",
                    "role": "USER"
                }
                """, "USERS");

        String userId = "userId";

        String userToken = Jwts.builder()
                .setSubject(userId)
                .claim("type", "access")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(172_800_000)))
                .signWith(JwtTokenService.SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();

        // Act
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/private/admin/any")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken));

        // Assert
        int statusCode = result.andReturn().getResponse().getStatus();

        assertThat(statusCode).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void shouldReturnUnauthorized_whenUserTriesToAccessPrivateRouteAndUserRoleIsNull() throws Exception {
        // Arrange
        String userId = "userId";

        String userToken = Jwts.builder()
                .setSubject(userId)
                .claim("type", "access")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(172_800_000)))
                .signWith(JwtTokenService.SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();

        // Act
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/private/any")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken));

        // Assert
        int statusCode = result.andReturn().getResponse().getStatus();

        assertThat(statusCode).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }



}




