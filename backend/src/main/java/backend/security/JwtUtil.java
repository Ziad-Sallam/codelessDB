package backend.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final String secret = "TEST SECRET SKJDBW;KBF/;NFE/WEJFNBE/WNF/EWJNFVKEJNFKJEWBFHEWB;";
    private final Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    // private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    // private final Key key = ;
    //  "SECRET KEY FOR TEST";

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Generate a token with userId + username
     */
    public String generateToken(int userId, String username) {

        Map<String, Object> claims = Map.of(
                "userId", userId,
                "username", username);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId)) // subject = "userId"
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    public int extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Integer.class));
    }

    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }

    public int extractSubject(String token) {
        return Integer.parseInt(extractClaim(token, Claims::getSubject));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token, int userId) {
        return extractSubject(token) == userId && !isTokenExpired(token);
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String refreshToken(String token) {
        int userId = extractUserId(token);
        String username = extractUsername(token);
        return generateToken(userId, username);
    }
}
