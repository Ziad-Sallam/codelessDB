package backend.collab;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import backend.collab.services.RoomManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
			// Consider closing the session or handling sessions without an ID
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

		if (diagramId != null) {
			log.info("Received binary message for Diagram ID {} with {} bytes.", diagramId, payload.remaining());

			// Broadcast the received message ONLY to clients in the same diagram/room
			broadcastBinaryMessage(diagramId, payload.array(), session.getId());
		
		} else {
			log.warn("Ignoring binary message from session {} as diagramId is missing.", session.getId());
		}
	}

	/**
	 * Broadcasts a raw binary message ONLY to clients in the specified room,
	 * excluding the sender.
	 * * @param diagramId The room/diagram ID to broadcast within.
	 * 
	 * @param data     The byte array to send.
	 * @param senderId The session ID of the sender to exclude from the broadcast.
	 */
	public void broadcastBinaryMessage(String diagramId, byte[] data, String senderId) {
		BinaryMessage message = new BinaryMessage(data);

		Set<WebSocketSession> roomSessions = roomManager.getSessionsInRoom(diagramId);

		if (roomSessions == null || roomSessions.isEmpty()) {
			log.warn("No sessions found for diagramId: {}", diagramId);
			return;
		}

		// Stream and send to the targeted room sessions
		roomSessions.parallelStream().forEach(session -> {
			if (session.isOpen() && !session.getId().equals(senderId)) {
				try {
					session.sendMessage(message);
				
				} catch (IOException e) {
					log.error("Error sending message to session {} in diagram {}:\n {}",
								 session.getId(), diagramId, e.getMessage());
				}
			}
		});
	}
}