package backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.stereotype.Service;

@Service
public class JwtExtractor {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Extracts, validates the token, and returns the AuthUser principal.
     * * @param authHeader The full Authorization header string 
     * (e.g., "Bearer <token>").
     * 
     * @return The AuthUser object if valid, or null if the header is
     *         missing/malformed.
     * @throws JwtValidationException if the token is invalid (expired, bad
     *                                signature, etc.).
     */
    public AuthUser authenticate(String tokenArg, boolean inHeader) throws JwtValidationException {
        // No JWT → return null (let the caller decide if auth is mandatory)
        if (tokenArg == null) return null;
        
        String token;
        if (inHeader) {
            if (!tokenArg.startsWith("Bearer "))
                return null;
            
            token = tokenArg.substring(7);
            
        } else {
            token = tokenArg;
        }

        Integer userId;
        String username;

        try {

            userId = jwtUtil.extractSubject(token);
            username = jwtUtil.extractUsername(token);

            // Validate the token using the primary check 
            // (can include expiration, signature, etc.)
            if (!jwtUtil.isTokenValid(token, userId)) {
                throw new JwtValidationException("Token failed validation check.", null);
            }

            return new AuthUser(userId, username);
        
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            throw new JwtValidationException("Token expired", null);
        
        } catch (io.jsonwebtoken.security.SignatureException ex) {
            throw new JwtValidationException("Invalid token signature", null);
        
        } catch (io.jsonwebtoken.MalformedJwtException ex) {
            throw new JwtValidationException("Malformed JWT token", null);
        
        } catch (IllegalArgumentException ex) {
            throw new JwtValidationException("Invalid or empty JWT token", null);
        
        } catch (Exception ex) {
            throw new JwtValidationException("Invalid token", null);
        }
    }
}