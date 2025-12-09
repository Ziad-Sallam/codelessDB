package backend.collab.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import backend.collab.exceptions.CollabException.CollaboratorsCapacityException;
import backend.config.ErrorResponse;
import backend.user.exceptions.UserException.EmailAlreadyExistsException;
import backend.user.exceptions.UserException.InvalidEmailException;
import backend.user.exceptions.UserException.InvalidTokenException;
import backend.user.exceptions.UserException.OtpSendFailedException;
import backend.user.exceptions.UserException.UserNotFoundException;
import backend.user.exceptions.UserException.UsernameAlreadyExistsException;
import io.jsonwebtoken.ExpiredJwtException;

@ControllerAdvice
public class CollabExceptionHandler {

   private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
      return new ResponseEntity<>(new ErrorResponse(message, status.value()), status);
   }

   @ExceptionHandler(CollaboratorsCapacityException.class)
   public ResponseEntity<ErrorResponse> handleUserNotFound(CollaboratorsCapacityException ex) {
      return build(HttpStatus.NOT_FOUND, ex.getMessage());
   }
}
