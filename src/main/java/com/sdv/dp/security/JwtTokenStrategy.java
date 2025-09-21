package com.sdv.dp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenStrategy implements TokenGenerationStrategy{
    private final SecretKey key;
    private final long expirationMillis;

    public JwtTokenStrategy(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMillis = expirationMinutes * 60_000;
    }

    @Override
    public String generate(UserDetails user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean isValid(String token, UserDetails user) {
        String username = extractUsername(token);
        return username != null && username.equals(user.getUsername()) && !isExpired(token);
    }

    @Override
    public String extractUsername(String token) {
        return parse(token).getBody().getSubject();
    }

    private boolean isExpired(String token) {
        Date exp = parse(token).getBody().getExpiration();
        return exp.before(new Date());
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
    }
}
