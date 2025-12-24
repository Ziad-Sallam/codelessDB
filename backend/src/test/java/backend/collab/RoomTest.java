package backend.collab;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import backend.collab.exceptions.CollabException.CollaboratorsCapacityException;
import backend.collab.snapshot.SnapshotService;
import backend.user.Role;

public class RoomTest {

	private Room room;
	private SnapshotService snapshotService;
	private String diagramId = "test-diagram-id";

	@BeforeEach
	void setUp() {
		snapshotService = mock(SnapshotService.class);
		room = new Room(diagramId, snapshotService);
	}

	@Test
	void testAddSession_Success() {
		WebSocketSession session = mock(WebSocketSession.class);
		room.addSession(session);
		assertFalse(room.isEmpty());
	}

	@Test
	void testAddSession_CapacityLimit() {
		// Fill the room up to MAX_COLLABORATORS (15)
		for (int i = 0; i < 15; i++) {
			room.addSession(mock(WebSocketSession.class));
		}

		WebSocketSession session = mock(WebSocketSession.class);
		assertThrows(CollaboratorsCapacityException.class, () -> room.addSession(session));
	}

	@Test
	void testRemoveSession() {
		WebSocketSession session = mock(WebSocketSession.class);
		room.addSession(session);
		assertFalse(room.isEmpty());

		boolean removed = room.removeSession(session);
		assertTrue(removed);
		assertTrue(room.isEmpty());
	}

	@Test
	void testDoUpdate_CursorUpdate_BroadcastsOnly() throws IOException {
		WebSocketSession sender = mock(WebSocketSession.class);
		when(sender.getId()).thenReturn("sender-1");
		when(sender.isOpen()).thenReturn(true);
		room.addSession(sender);

		WebSocketSession receiver = mock(WebSocketSession.class);
		when(receiver.getId()).thenReturn("receiver-1");
		when(receiver.isOpen()).thenReturn(true);
		room.addSession(receiver);

		// Update byte array: [0] = 1 means cursor update
		byte[] updateData = new byte[] { 1, 2, 3 };

		room.doUpdate(updateData, "sender-1", Role.WRITER);

		// Verification
		verify(snapshotService, never()).takeSnapshot(any());

		// Check broadcast
		ArgumentCaptor<BinaryMessage> messageCaptor = ArgumentCaptor.forClass(BinaryMessage.class);
		verify(receiver).sendMessage(messageCaptor.capture());

		// Sender should NOT receive the message
		verify(sender, never()).sendMessage(any());

		BinaryMessage sentMessage = messageCaptor.getValue();
		// Just verify buffer content roughly
		assertEquals(3, sentMessage.getPayload().capacity());
	}

	@Test
	void testDoUpdate_NormalUpdate_IncrementsCounter_AndSnapshots() throws IOException {
		WebSocketSession sender = mock(WebSocketSession.class);
		when(sender.getId()).thenReturn("sender-1");
		when(sender.isOpen()).thenReturn(true);
		room.addSession(sender);

		// Normal update: [0] != 1
		byte[] updateData = new byte[] { 0, 2, 3 };

		// Need 300 updates to trigger snapshot
		for (int i = 0; i < 299; i++) {
			room.doUpdate(updateData, "sender-1", Role.WRITER);
		}

		verify(snapshotService, never()).takeSnapshot(diagramId);

		// 300th update
		room.doUpdate(updateData, "sender-1", Role.WRITER);

		verify(snapshotService).takeSnapshot(diagramId);
	}

	@Test
	void testDoUpdate_ReaderCannotSendNormalUpdate() throws IOException {
		WebSocketSession sender = mock(WebSocketSession.class);
		when(sender.getId()).thenReturn("sender-1");
		room.addSession(sender);

		byte[] normalUpdate = new byte[] { 0, 2, 3 };

		WebSocketSession receiver = mock(WebSocketSession.class);
		when(receiver.getId()).thenReturn("receiver-1");
		when(receiver.isOpen()).thenReturn(true);
		room.addSession(receiver);

		room.doUpdate(normalUpdate, "sender-1", Role.READER);

		verify(receiver, never()).sendMessage(any());
	}

	@Test
	void testDoUpdate_ReaderCanSendCursorUpdate() throws IOException {
		WebSocketSession sender = mock(WebSocketSession.class);
		when(sender.getId()).thenReturn("sender-1");
		room.addSession(sender);

		WebSocketSession receiver = mock(WebSocketSession.class);
		when(receiver.getId()).thenReturn("receiver-1");
		when(receiver.isOpen()).thenReturn(true);
		room.addSession(receiver);

		byte[] cursorUpdate = new byte[] { 1, 2, 3 };

		room.doUpdate(cursorUpdate, "sender-1", Role.READER);

		verify(receiver).sendMessage(any(BinaryMessage.class));
	}

	@Test
	void testBroadcast_HandlesIOException() throws IOException {
		WebSocketSession sender = mock(WebSocketSession.class);
		when(sender.getId()).thenReturn("sender-1");
		room.addSession(sender);

		WebSocketSession receiver = mock(WebSocketSession.class);
		when(receiver.isOpen()).thenReturn(true);
		when(receiver.getId()).thenReturn("receiver-1");
		doThrow(new IOException("Connection error")).when(receiver).sendMessage(any());

		room.addSession(receiver);

		byte[] updateData = new byte[] { 1, 2, 3 };

		// Should not throw exception
		room.doUpdate(updateData, "sender-1", Role.WRITER);

		verify(receiver).sendMessage(any(BinaryMessage.class));
	}

	@Test
	void testNoSessions_DoUpdate_LogsWarnAndReturns() {
		// This tests the guard clause "if (this.sessions == null ||
		// this.sessions.isEmpty())"
		room.doUpdate(new byte[] { 0 }, "sender-1", Role.WRITER);
		// Nothing to verify except it doesn't crash
	}

	@Test
	void testClose_ClosesAllSessions() throws Exception {
		WebSocketSession s1 = mock(WebSocketSession.class);
		when(s1.isOpen()).thenReturn(true);
		when(s1.getId()).thenReturn("s1");

		WebSocketSession s2 = mock(WebSocketSession.class);
		when(s2.isOpen()).thenReturn(true);
		when(s2.getId()).thenReturn("s2");

		room.addSession(s1);
		room.addSession(s2);

		room.close();

		verify(s1).close(CloseStatus.NORMAL);
		verify(s2).close(CloseStatus.NORMAL);
		assertTrue(room.isEmpty());
	}

	@Test
	void testClose_HandlesExceptionDuringSessionClose() throws Exception {
		WebSocketSession s1 = mock(WebSocketSession.class);
		when(s1.isOpen()).thenReturn(true);
		doThrow(new RuntimeException("Close error")).when(s1).close(any());

		room.addSession(s1);

		room.close(); // Should not throw

		verify(s1).close(CloseStatus.NORMAL);
		assertTrue(room.isEmpty());
	}

	@Test
	void testTakeSnapshot() {
		room.takeSnapshot();
		verify(snapshotService).takeSnapshot(diagramId);
	}
}
