package backend.collab.exceptions;

public class CollabException {
	public static class DiagramNotFoundException extends RuntimeException {
		public DiagramNotFoundException(String message) {
			super(message);
		}
	}
}