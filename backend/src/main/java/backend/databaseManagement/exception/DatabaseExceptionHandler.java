package backend.databaseManagement.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import backend.databaseManagement.exception.DatabaseException.DatabaseNotConnectedException;
import backend.databaseManagement.exception.DatabaseException.DatabaseNotFoundException;
import backend.config.ErrorResponse;

@ControllerAdvice
public class DatabaseExceptionHandler {

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return new ResponseEntity<>(new ErrorResponse(message, status.value()), status);
    }

    @ExceptionHandler(DatabaseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseNotFound(DatabaseNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DatabaseNotConnectedException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseNotConnected(DatabaseNotConnectedException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

}
