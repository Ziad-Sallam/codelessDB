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

	// Store all active sessions for broadcasting
	private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

	@Autowired
	private RoomManager roomManager;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessions.add(session);
		String diagramId = (String) session.getAttributes().get("diagramId");
		log.info("New binary connection established with diagram: {}", session.getId());
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		sessions.remove(session);
		log.info("Binary connection closed: " + session.getId());
	}

	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
		// 1. Process the incoming raw binary message
		ByteBuffer payload = message.getPayload();
		String diagramId = (String) session.getAttributes().get("diagramId");
		log.info("Received binary message from {} with {} bytes.", diagramId, payload.remaining());

		// For a collaboration app, you might parse the payload to extract
		// information like "what change was made" and "by whom".

		// 2. Broadcast the received message to all other connected clients
		broadcastBinaryMessage(payload.array(), session.getId());
	}

	/**
	 * Broadcasts a raw binary message to all connected clients except the sender.
	 * 
	 * @param data     The byte array to send.
	 * @param senderId The session ID of the sender to exclude from the broadcast.
	 */
	public void broadcastBinaryMessage(byte[] data, String senderId) {
		BinaryMessage message = new BinaryMessage(data);

		sessions.parallelStream().forEach(session -> {
			if (session.isOpen() && !session.getId().equals(senderId)) {
				try {
					// Note: session.sendMessage is generally thread-safe,
					// but calling it concurrently on the same session
					// should be avoided. Using sessions.parallelStream()
					// with the synchronizedSet helps with safe iteration,
					// but you might consider making the actual sendMessage
					// call within a synchronized block if you hit concurrency issues
					// with the underlying WebSocket implementation.
					// roomManager
					session.sendMessage(message);
				} catch (IOException e) {
					log.error("Error sending message to session {} :\n {}", session.getId(), e.getMessage());
				}
			}
		});
	}
}