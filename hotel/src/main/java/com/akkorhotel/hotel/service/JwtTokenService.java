package com.akkorhotel.hotel.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

import static org.springframework.util.StringUtils.hasText;

@Component
@RequiredArgsConstructor
public class JwtTokenService {

    public static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private static final long TOKEN_EXPIRATION_TIME = 172_800_000;

    public String generateToken(String userId) {
        Instant now = Instant.now();
        Date expiryDate = Date.from(now.plusSeconds(TOKEN_EXPIRATION_TIME));

        return Jwts.builder()
                .setSubject(userId)
                .claim("type", "access")
                .setIssuedAt(Date.from(now))
                .setExpiration(expiryDate)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    public String resolveUserIdFromRequest(HttpServletRequest request) {
        String token = verifyTokenFormat(request);
        return token == null ? null : resolveUserIdFromToken(token);
    }

    public void generateEmailConfirmationToken() {

    }

    private String verifyTokenFormat(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            return null;
        }

        String token = bearerToken.substring(7);
        return isValidTokenFormat(token) ? token : null;
    }

    private boolean isValidTokenFormat(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);

            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private String resolveUserIdFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody().getSubject();
    }

}
