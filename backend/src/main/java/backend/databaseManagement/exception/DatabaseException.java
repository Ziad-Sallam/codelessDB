package backend.databaseManagement.exception;

public class DatabaseException {
    public static class DatabaseNotFoundException extends RuntimeException {
        public DatabaseNotFoundException(String message) {
            super(message);
        }
    }

    public static class ServerNotFoundException extends RuntimeException {
        public ServerNotFoundException(String message) {
            super(message);
        }
    }

    public static class DatabaseNotConnectedException extends RuntimeException {
        public DatabaseNotConnectedException(String message) {
            super(message);
        }
    }

    public static class DatabaseAlreadyExistsException extends RuntimeException {
        public DatabaseAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class ServerAlreadyExistsException extends RuntimeException {
        public ServerAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class MissingFieldException extends RuntimeException {
        public MissingFieldException(String message) {
            super(message);
        }
    }


}