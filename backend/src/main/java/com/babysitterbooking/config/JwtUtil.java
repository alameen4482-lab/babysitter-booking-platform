package com.babysitterbooking.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for JWT token operations.
 *
 * <p>Uses jjwt 0.12.x API (Jwts.builder(), Jwts.parser()).
 * The signing key is derived from the {@code app.jwt.secret} property.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Generate tokens with role claims</li>
 *   <li>Extract subject (email) from a token</li>
 *   <li>Validate token signature and expiry</li>
 * </ul>
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    // -------------------------------------------------------------------------
    // Token Generation
    // -------------------------------------------------------------------------

    /**
     * Generates a signed JWT for the given {@link UserDetails}.
     *
     * @param userDetails the authenticated user
     * @return signed JWT string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Embed role in claims for role-based authorization in downstream services
        userDetails.getAuthorities().stream()
                .findFirst()
                .ifPresent(authority -> claims.put("role", authority.getAuthority()));

        return buildToken(claims, userDetails.getUsername());
    }

    private String buildToken(Map<String, Object> extraClaims, String subject) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // -------------------------------------------------------------------------
    // Token Validation
    // -------------------------------------------------------------------------

    /**
     * Validates a token against the given {@link UserDetails}.
     * Checks both the username match and token expiry.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // -------------------------------------------------------------------------
    // Claims Extraction
    // -------------------------------------------------------------------------

    /**
     * Extracts the username (subject) from the token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // -------------------------------------------------------------------------
    // Signing Key
    // -------------------------------------------------------------------------

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
