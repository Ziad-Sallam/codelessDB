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
}
