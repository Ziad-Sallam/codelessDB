package backend.collab;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import org.springframework.web.socket.CloseStatus;

import backend.collab.services.RoomManager;
import backend.user.Role;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Component
@Slf4j
public class CollabWebSocketHandler extends BinaryWebSocketHandler {

	@Autowired
	private RoomManager roomManager;

	private String getDiagramId(WebSocketSession session) {
		return (String) session.getAttributes().get("diagramId");
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		String diagramId = getDiagramId(session);

		if (diagramId != null) {
			roomManager.joinRoom(diagramId, session);
			log.info("New binary connection established for Diagram ID: {}", diagramId);

		} else {
			log.warn("Session {} established without a valid diagramId.", session.getId());
			session.close(CloseStatus.BAD_DATA);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		String diagramId = getDiagramId(session);

		if (diagramId != null) {
			roomManager.leaveRoom(diagramId, session);
			log.info("Binary connection closed for Diagram ID: {}", diagramId);
		}
	}

	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
		ByteBuffer payload = message.getPayload();
		String diagramId = getDiagramId(session);
		Role role = (Role) session.getAttributes().get("role");

		if (diagramId == null) {
			log.warn("Ignoring binary message without diagramId");
			return;
		}

		roomManager.sendUpdate(diagramId, payload.array(), session.getId(), role);
	}

}