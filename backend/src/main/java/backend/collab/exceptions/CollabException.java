package backend.collab.exceptions;

public class CollabException {
	public static class CollaboratorsCapacityException extends RuntimeException {
		public CollaboratorsCapacityException(String message) {
			super(message);
		}
	}
}