package com.akkorhotel.hotel.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    @InjectMocks
    private JwtTokenService jwtTokenService;

    @Mock
    private HttpServletRequest request;

    @Test
    void shouldGenerateToken() {
        // Arrange
        String userId = "userId";

        // Act
        String token = jwtTokenService.generateToken(userId);

        // Assert
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(JwtTokenService.SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Date expirationDate = claims.getExpiration();
        String resolvedUserId = claims.getSubject();
        String tokenType = claims.get("type", String.class);

        assertThat(token).startsWith("eyJhbGciOiJIUzUxMiJ9.");
        assertThat(token.split("\\.").length == 3).isTrue();
        assertThat(expirationDate.after(new Date())).isTrue();
        assertThat(resolvedUserId).isEqualTo(userId);
        assertThat(tokenType).isEqualTo("access");
    }

    @Test
    void shouldResolveUserIdFromRequest() {
        // Arrange
        String userId = "userId";

        String token = Jwts.builder()
                .setSubject(userId)
                .claim("type", "access")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(172_800_000)))
                .signWith(JwtTokenService.SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // Act
        String resolvedUserId = jwtTokenService.resolveUserIdFromRequest(request);

        // Assert
        assertThat(resolvedUserId).isEqualTo(userId);
    }

    @Test
    void shouldReturnNull_whenTokenFromRequestIsNotPresent() {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        String resolvedUserId = jwtTokenService.resolveUserIdFromRequest(request);

        // Assert
        assertThat(resolvedUserId).isNull();
    }

    @Test
    void shouldReturnNull_whenTokenFromRequestIsNotBearerToken() {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");

        // Act
        String resolvedUserId = jwtTokenService.resolveUserIdFromRequest(request);

        // Assert
        assertThat(resolvedUserId).isNull();
    }

    @Test
    void shouldReturnNull_whenTokenFromRequestIsNotValidFormat() {
        // Arrange
        String invalidToken = "Bearer invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn(invalidToken);

        // Act
        String resolvedUserId = jwtTokenService.resolveUserIdFromRequest(request);

        // Assert
        assertThat(resolvedUserId).isNull();
    }

    @Test
    void shouldReturnNull_whenTokenFromRequestIsExpired() {
        // Arrange
        String userId = "userId";

        String expiredToken = Jwts.builder()
                .setSubject(userId)
                .claim("type", "access")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().minusSeconds(172_800_000)))
                .signWith(JwtTokenService.SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);

        // Act
        String resolvedUserId = jwtTokenService.resolveUserIdFromRequest(request);

        // Assert
        assertThat(resolvedUserId).isNull();
    }

    @Test
    void shouldGenerateEmailConfirmationToken() {
        // Arrange
        String userId = "userId";

        // Act
        String token = jwtTokenService.generateEmailConfirmationToken(userId);

        // Assert
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(JwtTokenService.SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Date expirationDate = claims.getExpiration();
        String resolvedUserId = claims.getSubject();
        String tokenType = claims.get("type", String.class);

        assertThat(token).startsWith("eyJhbGciOiJIUzUxMiJ9.");
        assertThat(token.split("\\.").length == 3).isTrue();
        assertThat(expirationDate.after(new Date())).isTrue();
        assertThat(resolvedUserId).isEqualTo(userId);
        assertThat(tokenType).isEqualTo("email_confirmation");
    }


}
