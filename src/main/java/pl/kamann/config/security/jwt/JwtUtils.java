package pl.kamann.config.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.kamann.entities.appuser.Role;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class JwtUtils {

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
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        List<String> roleNames = roles.stream().map(Role::getName).toList();

        String token = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .claim("roles", roleNames)
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        log.info("Generated token for {} with roles {}: {}", email, roleNames, token);

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

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}