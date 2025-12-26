package backend.user.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;

import backend.config.ErrorResponse;
import backend.user.exceptions.UserException.EmailAlreadyExistsException;
import backend.user.exceptions.UserException.InvalidEmailException;
import backend.user.exceptions.UserException.InvalidTokenException;
import backend.user.exceptions.UserException.OtpSendFailedException;
import backend.user.exceptions.UserException.UserNotFoundException;
import backend.user.exceptions.UserException.UsernameAlreadyExistsException;
import io.jsonwebtoken.ExpiredJwtException;

class UserExceptionHandlerTest {

    private UserExceptionHandler handler;

    @BeforeEach
    void setup() {
        handler = new UserExceptionHandler();
    }

    @Test
    void handleUserNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleUserNotFound(new UserNotFoundException("Not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().getMessage());
    }

    @Test
    void handleEmailExists() {
        ResponseEntity<ErrorResponse> response = handler.handleEmailExists(new EmailAlreadyExistsException("Exists"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleUsernameExists() {
        ResponseEntity<ErrorResponse> response = handler.handleUsernameExists(new UsernameAlreadyExistsException("Exists"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleInvalidEmail() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidEmail(new InvalidEmailException("Invalid"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleBadCredentials() {
        ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(new BadCredentialsException("Bad"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Wrong password", response.getBody().getMessage());
    }

    @Test
    void handleAccessDenied() {
        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(new AccessDeniedException("Denied"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleInvalidToken() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidToken(new InvalidTokenException("InvalidToken"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleExpiredToken() {
        ResponseEntity<ErrorResponse> response = handler.handleExpiredToken(mock(ExpiredJwtException.class));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult br = mock(BindingResult.class);
        FieldError fe = new FieldError("obj", "field", "default message");
        when(ex.getBindingResult()).thenReturn(br);
        when(br.getFieldErrors()).thenReturn(List.of(fe));

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("field: default message", response.getBody().getMessage());
    }

    @Test
    void handleValidation_noErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult br = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(br);
        when(br.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation error", response.getBody().getMessage());
    }

    @Test
    void handleMissingParams() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("param", "type");
        ResponseEntity<ErrorResponse> response = handler.handleMissingParams(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("param"));
    }

    @Test
    void handleBindingException() {
        ServletRequestBindingException ex = new ServletRequestBindingException("Binding error");
        ResponseEntity<ErrorResponse> response = handler.handleBindingException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Binding error", response.getBody().getMessage());
    }

    @Test
    void handleIllegalArgument() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(new IllegalArgumentException("Illegal"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleDataIntegrity() {
        DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
        Throwable cause = mock(Throwable.class);
        when(ex.getMostSpecificCause()).thenReturn(cause);
        when(cause.getMessage()).thenReturn("Integrity violation");
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Data conflict: Integrity violation", response.getBody().getMessage());
    }

    @Test
    void handleIllegalState() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalState(new IllegalStateException("State"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleUnsupported() {
        ResponseEntity<ErrorResponse> response = handler.handleUnsupported(new UnsupportedOperationException("Unsupported"));
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
    }

    @Test
    void handleOtpSendFailed() {
        ResponseEntity<ErrorResponse> response = handler.handleOtpSendFailed(new OtpSendFailedException("Failed"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    void handleAll() {
        ResponseEntity<ErrorResponse> response = handler.handleAll(new Exception("Generic"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Generic", response.getBody().getMessage());
    }

    @Test
    void handleAll_nullMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleAll(new Exception((String) null));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().getMessage());
    }
}
