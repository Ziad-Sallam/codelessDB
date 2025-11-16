package backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

	private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
	private final long jwtExpirationMs = 24 * 60 * 60 * 1000; // 1 day

	public String generateToken(String subject, Map<String, Object> extraClaims) {
		return Jwts.builder()
				.setClaims(extraClaims)
				.setSubject(subject)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
				.signWith(key)
				.compact();
	}

	public String generateToken(String subject) {
		return generateToken(subject, Map.of());
	}

	// ------------------- Extract Data from JWT -------------------
	public String extractSubject(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	// ------------------- Validate JWT -------------------
	public boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	public boolean isTokenValid(String token, String subject) {
		return extractSubject(token).equals(subject) && !isTokenExpired(token);
	}

	public boolean isTokenValid(String token) {
		try {
			extractAllClaims(token);
			return true;
		
		} catch (JwtException e) {
			return false;
		}
	}
}
