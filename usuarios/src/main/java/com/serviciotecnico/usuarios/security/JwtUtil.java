package com.serviciotecnico.usuarios.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET = "claveSecretaMuySeguraParaJWT2024!!";
    private static final long EXPIRACION_MS = 86400000; // 24 horas

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generarToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRACION_MS))
                .signWith(key)
                .compact();
    }

}
