package backend.collab.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import backend.config.ErrorResponse;

class CollabExceptionHandlerTest {

	private final CollabExceptionHandler handler = new CollabExceptionHandler();

	@Test
	void testHandleRoomCapacityExceed() {
		CollabException.CollaboratorsCapacityException ex = new CollabException.CollaboratorsCapacityException("Full");
		ResponseEntity<ErrorResponse> response = handler.handleRoomCapacityExceed(ex);

		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Full", response.getBody().getMessage());
		assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getBody().getStatus());
	}

	@Test
	void testHandleYDocInvalidUpdate() {
		CollabException.YDocUpdateException ex = new CollabException.YDocUpdateException("Bad update");
		ResponseEntity<ErrorResponse> response = handler.handleYDocInvalidUpdate(ex);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Bad update", response.getBody().getMessage());
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
	}

	@Test
	void testHandleRoomNotFound() {
		CollabException.RoomNotFoundException ex = new CollabException.RoomNotFoundException("Missing");
		ResponseEntity<ErrorResponse> response = handler.handleRoomNotFound(ex);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Missing", response.getBody().getMessage());
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
	}
}
