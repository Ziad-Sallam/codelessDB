package backend.collab.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import backend.config.ErrorResponse;
import backend.user.exceptions.UserException.EmailAlreadyExistsException;
import backend.user.exceptions.UserException.InvalidEmailException;
import backend.user.exceptions.UserException.InvalidTokenException;
import backend.user.exceptions.UserException.OtpSendFailedException;
import backend.user.exceptions.UserException.UserNotFoundException;
import backend.user.exceptions.UserException.UsernameAlreadyExistsException;
import io.jsonwebtoken.ExpiredJwtException;

@ControllerAdvice
public class UserExceptionHandler {

   private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
      return new ResponseEntity<>(new ErrorResponse(message, status.value()), status);
   }

   @ExceptionHandler(UserNotFoundException.class)
   public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
      return build(HttpStatus.NOT_FOUND, ex.getMessage());
   }

   @ExceptionHandler(EmailAlreadyExistsException.class)
   public ResponseEntity<ErrorResponse> handleEmailExists(EmailAlreadyExistsException ex) {
      return build(HttpStatus.CONFLICT, ex.getMessage());
   }

   @ExceptionHandler(UsernameAlreadyExistsException.class)
   public ResponseEntity<ErrorResponse> handleUsernameExists(UsernameAlreadyExistsException ex) {
      return build(HttpStatus.CONFLICT, ex.getMessage());
   }

   @ExceptionHandler(InvalidEmailException.class)
   public ResponseEntity<ErrorResponse> handleInvalidEmail(InvalidEmailException ex) {
      return build(HttpStatus.BAD_REQUEST, ex.getMessage());
   }

   @ExceptionHandler(BadCredentialsException.class)
   public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
      return build(HttpStatus.UNAUTHORIZED, "Invalid username or password");
   }

   @ExceptionHandler(AccessDeniedException.class)
   public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
      return build(HttpStatus.FORBIDDEN, "You are not authorized to perform this action");
   }

   @ExceptionHandler(InvalidTokenException.class)
   public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex) {
      return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
   }

   @ExceptionHandler(ExpiredJwtException.class)
   public ResponseEntity<ErrorResponse> handleExpiredToken(ExpiredJwtException ex) {
      return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
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

   @ExceptionHandler(IllegalArgumentException.class)
   public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
      return build(HttpStatus.BAD_REQUEST, ex.getMessage());
   }

   @ExceptionHandler(DataIntegrityViolationException.class)
   public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
      return build(HttpStatus.CONFLICT, "Data conflict: " + ex.getMostSpecificCause().getMessage());
   }

   @ExceptionHandler(IllegalStateException.class)
   public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
      return build(HttpStatus.CONFLICT, ex.getMessage());
   }

   @ExceptionHandler(UnsupportedOperationException.class)
   public ResponseEntity<ErrorResponse> handleUnsupported(UnsupportedOperationException ex) {
      return build(HttpStatus.NOT_ACCEPTABLE, ex.getMessage());
   }

   @ExceptionHandler(OtpSendFailedException.class)
   public ResponseEntity<ErrorResponse> handleOtpSendFailed(OtpSendFailedException ex) {
      return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
   }

   @ExceptionHandler(Exception.class)
   public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
      // ex.printStackTrace();
      return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
   }
}
