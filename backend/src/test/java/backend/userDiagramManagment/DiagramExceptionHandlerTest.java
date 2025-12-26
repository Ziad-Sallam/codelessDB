package backend.userDiagramManagment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import backend.userDiagramManagement.exceptions.DiagramExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import backend.config.ErrorResponse;
import backend.userDiagramManagement.exceptions.DiagramException.DiagramNotFoundException;

class DiagramExceptionHandlerTest {

    private final DiagramExceptionHandler handler = new DiagramExceptionHandler();

    @Test
    void handleValidation_withFieldErrors() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "default message");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // Using a constructor that works without complex reflection for a unit test
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException((MethodParameter)null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("field: default message", response.getBody().getMessage());
    }

    @Test
    void handleValidation_withoutFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException((MethodParameter)null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation error", response.getBody().getMessage());
    }

    @Test
    void handleDataIntegrity() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Integrity error", new RuntimeException("Specific cause"));
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(ex);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertEquals("Data conflict: Specific cause", response.getBody().getMessage());
    }

    @Test
    void handleIllegalState() {
        IllegalStateException ex = new IllegalStateException("Illegal state");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalState(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Illegal state", response.getBody().getMessage());
    }

    @Test
    void handleUnsupported() {
        UnsupportedOperationException ex = new UnsupportedOperationException("Unsupported");
        ResponseEntity<ErrorResponse> response = handler.handleUnsupported(ex);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertEquals("Unsupported", response.getBody().getMessage());
    }

    @Test
    void handleDiagramNotFound() {
        DiagramNotFoundException ex = new DiagramNotFoundException("Not found");
        ResponseEntity<ErrorResponse> response = handler.handleDiagramNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().getMessage());
    }
}
