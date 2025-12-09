package backend.SQLOptimization.exceptions;

public class SQLOptimizationException {

    public static class GeminiAPIException extends RuntimeException {
        public GeminiAPIException(String message) {
            super(message);
        }
    }

    public static class InvalidSQLException extends RuntimeException {
        public InvalidSQLException(String message) {
            super(message);
        }
    }

    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) {
            super(message);
        }
    }
}

