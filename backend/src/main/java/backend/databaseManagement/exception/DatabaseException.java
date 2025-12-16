package backend.databaseManagement.exception;

public class DatabaseException {
    public static class DatabaseNotFoundException extends RuntimeException {
        public DatabaseNotFoundException(String message) {
            super(message);
        }
    }

    public static class DatabaseNotConnectedException extends RuntimeException {
        public DatabaseNotConnectedException(String message) {
            super(message);
        }
    }

}
