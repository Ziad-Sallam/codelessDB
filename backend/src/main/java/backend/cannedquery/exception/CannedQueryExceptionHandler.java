package backend.cannedquery.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import backend.cannedquery.exception.CannedQueryException.CannedQueryAlreadyExistsException;
import backend.cannedquery.exception.CannedQueryException.CannedQueryNotFoundException;

@ControllerAdvice
public class CannedQueryExceptionHandler {
    @ExceptionHandler(CannedQueryNotFoundException.class)
    public ResponseEntity<String> handleCannedQueryNotFound(CannedQueryNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CannedQueryAlreadyExistsException.class)
    public ResponseEntity<String> handleCannedQueryAlreadyExists(CannedQueryAlreadyExistsException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

}
