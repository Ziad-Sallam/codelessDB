package backend.SQLOptimization.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import backend.config.ErrorResponse;
import backend.SQLOptimization.exceptions.SQLOptimizationException.GeminiAPIException;
import backend.SQLOptimization.exceptions.SQLOptimizationException.InvalidSQLException;
import backend.SQLOptimization.exceptions.SQLOptimizationException.RateLimitExceededException;

@ControllerAdvice
public class SQLOptimizationExceptionHandler {

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return new ResponseEntity<>(new ErrorResponse(message, status.value()), status);
    }

    @ExceptionHandler(InvalidSQLException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSQL(InvalidSQLException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(GeminiAPIException.class)
    public ResponseEntity<ErrorResponse> handleGeminiAPI(GeminiAPIException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(RateLimitExceededException ex) {
        return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }
}

