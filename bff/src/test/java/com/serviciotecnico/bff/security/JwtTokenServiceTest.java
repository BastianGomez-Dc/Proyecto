package com.serviciotecnico.bff.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-32b!";

    @Test
    void extractSubject_returnsUsernameForValidToken() {
        JwtTokenService service = new JwtTokenService(SECRET);
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("admin")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key)
                .compact();

        assertEquals("admin", service.extractSubject(token));
    }

    @Test
    void extractSubject_throwsForTokenSignedWithDifferentSecret() {
        JwtTokenService service = new JwtTokenService(SECRET);
        SecretKey otherKey = Keys.hmacShaKeyFor("a-completely-different-secret-key-value".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("admin")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(otherKey)
                .compact();

        assertThrows(JwtException.class, () -> service.extractSubject(token));
    }

    @Test
    void extractSubject_throwsForMalformedToken() {
        JwtTokenService service = new JwtTokenService(SECRET);

        assertThrows(JwtException.class, () -> service.extractSubject("not-a-jwt"));
    }
}
