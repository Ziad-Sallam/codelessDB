package backend.databaseManagement.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import backend.databaseManagement.exception.DatabaseException.DatabaseNotConnectedException;
import backend.databaseManagement.exception.DatabaseException.DatabaseNotFoundException;
import backend.databaseManagement.exception.DatabaseException.DatabaseAlreadyExistsException;
import backend.databaseManagement.exception.DatabaseException.MissingFieldException;
import backend.databaseManagement.exception.DatabaseException.ServerAlreadyExistsException;
import backend.databaseManagement.exception.DatabaseException.ServerNotFoundException;
import backend.databaseManagement.exception.DatabaseException.UnauthorizedAccessException;
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

    @ExceptionHandler(ServerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleServerNotFound(ServerNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DatabaseAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseAlreadyExists(DatabaseAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ServerAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleServerAlreadyExists(ServerAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MissingFieldException.class)
    public ResponseEntity<ErrorResponse> handleMissingField(MissingFieldException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");
    }

}
