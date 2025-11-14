package backend.user.exceptions;

public interface UserExceptions {}

class UserNotFoundException extends RuntimeException {
	public UserNotFoundException(String message) {
		super(message);
	}
}

class EmailAlreadyExistsException extends RuntimeException {
	public EmailAlreadyExistsException(String message) {
		super(message);
	}
}

class InvalidTokenException extends RuntimeException {
	public InvalidTokenException(String message) {
		super(message);
	}
}

class ExpiredTokenException extends RuntimeException {
	public ExpiredTokenException(String message) {
		super(message);
	}
}