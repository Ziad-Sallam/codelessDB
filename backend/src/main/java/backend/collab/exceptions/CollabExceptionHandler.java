package backend.collab.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import backend.collab.exceptions.CollabException.CollaboratorsCapacityException;
import backend.collab.exceptions.CollabException.YDocUpdateException;
import backend.config.ErrorResponse;

@ControllerAdvice
public class CollabExceptionHandler {

   private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
      return new ResponseEntity<>(new ErrorResponse(message, status.value()), status);
   }

   @ExceptionHandler(CollaboratorsCapacityException.class)
   public ResponseEntity<ErrorResponse> handleUserNotFound(CollaboratorsCapacityException ex) {
      return build(HttpStatus.NOT_ACCEPTABLE, ex.getMessage());
   }

   @ExceptionHandler(YDocUpdateException.class)
   public ResponseEntity<ErrorResponse> handleUserNotFound(YDocUpdateException ex) {
      return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
   }
}
