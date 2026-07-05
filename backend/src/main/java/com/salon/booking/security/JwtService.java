package com.salon.booking.security;

import com.salon.booking.config.SalonProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

/** Issues and verifies the application's own JWTs (customer + admin sessions). */
@Service
public class JwtService {

    private final SecretKey key;
    private final long ttlMinutes;

    public JwtService(SalonProperties props) {
        String secret = props.getAuth().getJwtSecret();
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlMinutes = props.getAuth().getJwtTtlMinutes();
    }

    public String issue(String subject, String role, Long customerId, String name, String email) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttlMinutes, ChronoUnit.MINUTES)))
                .claim("role", role)
                .claim("name", name)
                .claim("email", email);
        if (customerId != null) {
            builder.claim("cid", customerId);
        }
        return builder.signWith(key).compact();
    }

    /** Parses and verifies a token, throwing if invalid/expired. */
    public AuthPrincipal parse(String token) {
        Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        Claims c = jws.getPayload();
        Object cidRaw = c.get("cid");
        Long cid = (cidRaw instanceof Number n) ? n.longValue() : null;
        return new AuthPrincipal(
                c.get("role", String.class),
                cid,
                c.get("email", String.class),
                c.get("name", String.class));
    }
}
