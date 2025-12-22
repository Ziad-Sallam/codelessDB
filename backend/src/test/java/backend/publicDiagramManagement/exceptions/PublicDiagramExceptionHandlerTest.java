package backend.publicDiagramManagement.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import backend.config.ErrorResponse;

class PublicDiagramExceptionHandlerTest {

    private PublicDiagramExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PublicDiagramExceptionHandler();
    }

    @Test
    void handleDiagramNotFound() {
        PublicDiagramException.DiagramNotFoundException ex = new PublicDiagramException.DiagramNotFoundException("not found");
        ResponseEntity<ErrorResponse> response = handler.handleDiagramNotFound(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("not found", response.getBody().getMessage());
    }

    @Test
    void handlePermissionDenied() {
        PublicDiagramException.PermissionDeniedException ex = new PublicDiagramException.PermissionDeniedException("denied");
        ResponseEntity<ErrorResponse> response = handler.handlePermissionDenied(ex);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("denied", response.getBody().getMessage());
    }

    @Test
    void handleValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "error message");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("field: error message"));
    }

    @Test
    void handleDataIntegrity() {
        DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
        RuntimeException cause = new RuntimeException("constraint violated");
        when(ex.getMostSpecificCause()).thenReturn(cause);
        
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(ex);
        
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("constraint violated"));
    }

    @Test
    void handleIllegalState() {
        IllegalStateException ex = new IllegalStateException("illegal state");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalState(ex);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("illegal state", response.getBody().getMessage());
    }

    @Test
    void handleUnsupported() {
        UnsupportedOperationException ex = new UnsupportedOperationException("unsupported");
        ResponseEntity<ErrorResponse> response = handler.handleUnsupported(ex);
        
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertEquals("unsupported", response.getBody().getMessage());
    }
}
