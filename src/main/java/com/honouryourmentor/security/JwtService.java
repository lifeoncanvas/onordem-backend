package com.honouryourmentor.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.access-token-expiration-ms:86400000}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateAccessToken(UserDetails userDetails, String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return buildToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails.getUsername(), refreshTokenExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Secrets that were ever committed to the repository. They are public, so refuse to sign with them. */
    private static final java.util.Set<String> KNOWN_LEAKED_SECRETS = java.util.Set.of(
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
    );

    private SecretKey signingKey;

    @jakarta.annotation.PostConstruct
    void initSigningKey() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("JWT_SECRET is not set. Provide a random secret of at least 32 bytes.");
        }
        if (KNOWN_LEAKED_SECRETS.contains(secretKey.trim())) {
            throw new IllegalStateException("JWT_SECRET is a publicly known value from the repository. Generate a new one.");
        }
        // Same decoding rule as before, so tokens signed with an existing valid secret stay valid.
        byte[] keyBytes = Decoders.BASE64.decode(secretKey.length() % 4 != 0 ?
                java.util.Base64.getEncoder().encodeToString(secretKey.getBytes()) : secretKey);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET is too short: it must decode to at least 32 bytes (256 bits).");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getSignInKey() {
        return signingKey;
    }
}
