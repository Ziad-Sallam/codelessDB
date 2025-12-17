package backend.collab.exceptions;

public class CollabException {
	public static class CollaboratorsCapacityException extends RuntimeException {
		public CollaboratorsCapacityException(String message) {
			super(message);
		}
	}

	public static class YDocUpdateException extends RuntimeException {
		public YDocUpdateException(String message) {
			super(message);
		}
	}

	public static class RoomNotFoundException extends RuntimeException {
		public RoomNotFoundException(String message) {
			super(message);
		}
	}
}