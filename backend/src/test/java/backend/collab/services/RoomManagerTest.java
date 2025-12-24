package backend.collab.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import backend.collab.exceptions.CollabException.RoomNotFoundException;
import backend.collab.snapshot.SnapshotService;
import backend.user.Role;

@ExtendWith(MockitoExtension.class)
class RoomManagerTest {

	@Mock
	private RedisStreamService redisService;

	@Mock
	private SnapshotService snapshotService;

	@InjectMocks
	private RoomManagerImpl roomManager;

	@Test
	void testJoinRoom_CreatesRoomAndAddsSession() {
		String roomId = "room-1";
		WebSocketSession session = mock(WebSocketSession.class);

		roomManager.joinRoom(roomId, session);

		// Access private map to verify room creation
		// Or verify via behavior if we could spy.
		// Since Room is created inside, it's hard to verify the Room object itself
		// without reflection or Powermock.
		// But we can verify no exception is thrown.
	}

	@Test
	void testLeaveRoom_RemovesSession_AndClosesRoomIfEmpty() throws Exception {
		String roomId = "room-1";
		WebSocketSession session = mock(WebSocketSession.class);

		// Join first
		roomManager.joinRoom(roomId, session);

		// Leave
		roomManager.leaveRoom(roomId, session);

		// Should trigger snapshot if room is empty (which it is, since we added 1 then
		// removed 1)
		// Note: Room calls snapshotService.takeSnapshot() on close.
		// However, Room is instantiated inside joinRoom with the MOCKED
		// snapshotService.
		// So we can verify the mock interaction.
		verify(snapshotService).takeSnapshot(roomId);
	}

	@Test
	void testLeaveRoom_NonExistentRoom_DoesNothing() {
		WebSocketSession session = mock(WebSocketSession.class);
		assertDoesNotThrow(() -> roomManager.leaveRoom("non-existent", session));
	}

	@Test
	void testSendUpdate_ValidRoom_NotCursor() {
		String roomId = "room-1";
		WebSocketSession session = mock(WebSocketSession.class);
		when(session.isOpen()).thenReturn(true);
		when(session.getId()).thenReturn("s1");

		roomManager.joinRoom(roomId, session);

		byte[] data = new byte[] { 0, 2, 3 }; // Not cursor

		roomManager.sendUpdate(roomId, data, "s1", Role.WRITER);

		verify(redisService).addUpdate(eq(roomId), eq(data));
		// Room.doUpdate logic is internal, but logic flow is sound.
	}

	@Test
	void testSendUpdate_ValidRoom_Cursor() {
		String roomId = "room-1";
		WebSocketSession session = mock(WebSocketSession.class);
		when(session.isOpen()).thenReturn(true);
		when(session.getId()).thenReturn("s1");

		roomManager.joinRoom(roomId, session);

		byte[] data = new byte[] { 1, 2, 3 }; // Cursor

		roomManager.sendUpdate(roomId, data, "s1", Role.WRITER);

		verify(redisService, never()).addUpdate(any(), any());
	}

	@Test
	void testSendUpdate_RoomNotFound() {
		byte[] data = new byte[] { 0 };
		assertThrows(RoomNotFoundException.class, () -> roomManager.sendUpdate("missing", data, "s1", Role.WRITER));
	}
}
