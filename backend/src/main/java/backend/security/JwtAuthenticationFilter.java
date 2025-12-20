package backend.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.config.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtExtractor jwtExtractor;

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

        // The filter should only proceed if the security context is NOT set.
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                AuthUser authUser = jwtExtractor.authenticate(authHeader, true);

                if (authUser != null) {
                    // Token is valid; set authentication
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            authUser,
                            null,
                            null
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtValidationException ex) {
                // Handle the validation failure by sending a 401 response
                writeUnauthorized(response, ex.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
