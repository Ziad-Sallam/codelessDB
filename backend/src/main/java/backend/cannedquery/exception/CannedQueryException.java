package backend.cannedquery.exception;

public class CannedQueryException {
    public static class CannedQueryNotFoundException extends RuntimeException {
        public CannedQueryNotFoundException(String message) {
            super(message);
        }
    }

    public static class CannedQueryAlreadyExistsException extends RuntimeException {
        public CannedQueryAlreadyExistsException(String message) {
            super(message);
        }
    }
    
}