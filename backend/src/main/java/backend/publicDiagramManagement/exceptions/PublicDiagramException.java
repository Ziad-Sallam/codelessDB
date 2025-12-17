package backend.publicDiagramManagement.exceptions;

public class PublicDiagramException {

    private PublicDiagramException() {
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

    public static class AlreadyExistsException extends RuntimeException {
        public AlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class PublishFailedException extends RuntimeException {
        public PublishFailedException(String message) {
            super(message);
        }
    }

    public static class NotPublicDiagramException extends RuntimeException {
        public NotPublicDiagramException(String message) {
            super(message);
        }
    }
}
