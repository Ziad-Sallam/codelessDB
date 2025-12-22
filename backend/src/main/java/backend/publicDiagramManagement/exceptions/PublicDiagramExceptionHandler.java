package backend.publicDiagramManagement.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import backend.config.ErrorResponse;
import backend.publicDiagramManagement.exceptions.PublicDiagramException.DiagramNotFoundException;
import backend.publicDiagramManagement.exceptions.PublicDiagramException.PermissionDeniedException;

@ControllerAdvice
public class PublicDiagramExceptionHandler {

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return new ResponseEntity<>(new ErrorResponse(message, status.value()), status);
    }

    @ExceptionHandler(DiagramNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDiagramNotFound(DiagramNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ErrorResponse> handlePermissionDenied(PermissionDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        return build(HttpStatus.NOT_ACCEPTABLE, "Data conflict: " + ex.getMostSpecificCause().getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnsupported(UnsupportedOperationException ex) {
        return build(HttpStatus.NOT_ACCEPTABLE, ex.getMessage());
    }
}
