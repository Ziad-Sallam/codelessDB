package backend.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.config.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Write a 401 Unauthorized JSON response.
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        ErrorResponse err = new ErrorResponse(message, HttpStatus.UNAUTHORIZED.value());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        new ObjectMapper().writeValue(response.getWriter(), err);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return "OPTIONS".equalsIgnoreCase(request.getMethod()) ||
                path.startsWith("/user/login") ||
                path.startsWith("/user/signup") ||
                path.startsWith("/oauth2") ||
                path.startsWith("/login/oauth2");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // No JWT → continue normally
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token;
        Integer userId;
        String username;

        try {
            token = authHeader.substring(7);
            log.error("## Extract token");

            userId = jwtUtil.extractSubject(token);
            log.error("## Extract UserId");

            username = jwtUtil.extractUsername(token);
            log.error("## Extract Username");

        } catch (ExpiredJwtException ex) {
            log.error("JWT expired: {}", ex.getMessage());
            writeUnauthorized(response, "Token expired");
            return;

        } catch (SignatureException ex) {
            log.error("Invalid token signature: {}", ex.getMessage());
            writeUnauthorized(response, "Invalid token signature");
            return;

        } catch (MalformedJwtException ex) {
            log.error("Malformed token: {}", ex.getMessage());
            writeUnauthorized(response, "Malformed JWT token");
            return;

        } catch (IllegalArgumentException ex) {
            log.error("Invalid token: {}", ex.getMessage());
            writeUnauthorized(response, "Invalid or empty JWT token");
            return;

        } catch (Exception ex) {
            log.error("Unknown JWT error: {}", ex.getMessage());
            writeUnauthorized(response, "Invalid token");
            return;
        }

        // Validate token normally
        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtUtil.isTokenValid(authHeader.substring(7), userId)) {
                log.error("## Token is valid");

                AuthUser authUser = new AuthUser(userId, username);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        authUser,
                        null,
                        null // or your roles
                );

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
