package backend.user.exceptions;

public class UserException {

	public static class UserNotFoundException extends RuntimeException {
		public UserNotFoundException(String message) {
			super(message);
		}
	}

	public static class EmailAlreadyExistsException extends RuntimeException {
		public EmailAlreadyExistsException(String message) {
			super(message);
		}
	}
	
	public static class InvalidEmailException extends RuntimeException {
		public InvalidEmailException(String message) {
			super(message);
		}
	}
	
	public static class UsernameAlreadyExistsException extends RuntimeException {
		public UsernameAlreadyExistsException(String message) {
			super(message);
		}
	}

	public static class InvalidTokenException extends RuntimeException {
		public InvalidTokenException(String message) {
			super(message);
		}
	}
}