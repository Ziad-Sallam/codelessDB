package backend.userDiagramManagement.exceptions;

public class DiagramException {

    private DiagramException() {
    }

    public static class DiagramNotFoundException extends RuntimeException {
        public DiagramNotFoundException(String message) {
            super(message);
        }
    }

    public static class PermissionDeniedException extends RuntimeException {
        public PermissionDeniedException(String message) {
            super(message);
        }
    }

    public static class InvalidDiagramDataException extends RuntimeException {
        public InvalidDiagramDataException(String message) {
            super(message);
        }
    }
}