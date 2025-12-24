package backend.collab.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CollabExceptionTest {

	@Test
	void testCollaboratorsCapacityException() {
		String message = "Full";
		CollabException.CollaboratorsCapacityException ex = new CollabException.CollaboratorsCapacityException(message);
		assertEquals(message, ex.getMessage());
	}

	@Test
	void testYDocUpdateException() {
		String message = "Update error";
		CollabException.YDocUpdateException ex = new CollabException.YDocUpdateException(message);
		assertEquals(message, ex.getMessage());
	}

	@Test
	void testRoomNotFoundException() {
		String message = "Not found";
		CollabException.RoomNotFoundException ex = new CollabException.RoomNotFoundException(message);
		assertEquals(message, ex.getMessage());
	}
}
