package pl.kamann.config.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.kamann.entities.appuser.Role;
import pl.kamann.entities.appuser.TokenType;

import javax.crypto.SecretKey;
import java.util.*;

@Slf4j
@Component
public class JwtUtils {

    @Getter
    private final SecretKey secretKey;
    private final long jwtExpiration;

    public JwtUtils(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:36000000}") long jwtExpiration // default 10 hours
    ) {
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(decodedKey);
        this.jwtExpiration = jwtExpiration;
    }

    public String generateToken(String email, Set<Role> roles) {
        Map<String, Object> claims = createClaims("roles", roles.stream().map(Role::getName).toList());
        return generateTokenWithClaims(email, claims, jwtExpiration);
    }

    public String generateTokenWithFlag(String email, TokenType flag, long expirationTime) {
        Map<String, Object> claims = createClaims("TokenType", flag.toString());

        return generateTokenWithClaims(email, claims, expirationTime);
    }

    private Map<String, Object> createClaims(String key, Object value) {
        return Collections.singletonMap(key, value);
    }

    public String generateTokenWithClaims(String email, Map<String, Object> claims, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        String token = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        log.info("Generated token for {}", email);

        return token;
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.error("Failed to extract claims from token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            return !isTokenExpired(token);
        } catch (JwtException e) {
            log.error("JWT validation failed: {}", e.getMessage());
        }
        return false;
    }

    public boolean isTokenTypeValid(String token, TokenType expectedTokenType) {
        String tokenTypeString = extractClaim(token, claims -> claims.get("TokenType", String.class));

        TokenType tokenType = TokenType.valueOf(tokenTypeString);

        return tokenType.equals(expectedTokenType);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    public Optional<String> extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return (bearerToken != null && bearerToken.startsWith("Bearer "))
                ? Optional.of(bearerToken.substring(7))
                : Optional.empty();
    }
}