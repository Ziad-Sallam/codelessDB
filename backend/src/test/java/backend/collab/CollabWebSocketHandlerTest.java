package backend.collab;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import backend.collab.services.RoomManager;
import backend.user.Role;

@ExtendWith(MockitoExtension.class)
class CollabWebSocketHandlerTest {

	@Mock
	private RoomManager roomManager;

	@InjectMocks
	private CollabWebSocketHandler handler;

	private WebSocketSession session;
	private Map<String, Object> attributes;

	@BeforeEach
	void setUp() {
		session = mock(WebSocketSession.class);
		attributes = new HashMap<>();
		when(session.getAttributes()).thenReturn(attributes);
	}

	@Test
	void testAfterConnectionEstablished_WithValidDiagramId() throws Exception {
		String diagramId = "test-diagram";
		attributes.put("diagramId", diagramId);

		handler.afterConnectionEstablished(session);

		verify(roomManager).joinRoom(diagramId, session);
	}

	@Test
	void testAfterConnectionEstablished_WithMissingDiagramId() throws Exception {
		// diagramId is null
		when(session.getId()).thenReturn("sess-1");

		handler.afterConnectionEstablished(session);

		verify(roomManager, never()).joinRoom(anyString(), any());
		verify(session).close(CloseStatus.BAD_DATA);
	}

	@Test
	void testAfterConnectionClosed_WithValidDiagramId() throws Exception {
		String diagramId = "test-diagram";
		attributes.put("diagramId", diagramId);

		handler.afterConnectionClosed(session, CloseStatus.NORMAL);

		verify(roomManager).leaveRoom(diagramId, session);
	}

	@Test
	void testAfterConnectionClosed_WithMissingDiagramId() throws Exception {
		// diagramId is null
		handler.afterConnectionClosed(session, CloseStatus.NORMAL);

		verify(roomManager, never()).leaveRoom(anyString(), any());
	}

	@Test
	void testHandleBinaryMessage_WithValidDiagramId() throws Exception {
		String diagramId = "test-diagram";
		Role role = Role.WRITER;
		attributes.put("diagramId", diagramId);
		attributes.put("role", role);
		when(session.getId()).thenReturn("sess-1");

		byte[] data = new byte[] { 1, 2, 3 };
		BinaryMessage message = new BinaryMessage(data);

		handler.handleBinaryMessage(session, message);

		verify(roomManager).sendUpdate(eq(diagramId), any(byte[].class), eq("sess-1"), eq(role));
	}

	@Test
	void testHandleBinaryMessage_WithMissingDiagramId() throws Exception {
		// session has no diagramId
		byte[] data = new byte[] { 1, 2, 3 };
		BinaryMessage message = new BinaryMessage(data);

		handler.handleBinaryMessage(session, message);

		verify(roomManager, never()).sendUpdate(anyString(), any(), anyString(), any());
	}
}
