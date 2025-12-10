package backend.collab;

import java.io.Closeable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.web.socket.WebSocketSession;

import backend.collab.exceptions.CollabException.CollaboratorsCapacityException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j; // Recommended for logging instead of direct System.out

import org.springframework.web.socket.WebSocketSession;

interface IRoom extends Closeable {

	void addSession(WebSocketSession session);
	
	boolean removeSession(WebSocketSession session);
	
	boolean isEmpty();
}

@Slf4j
@Getter
public class Room implements IRoom {
	
	private static final int MAX_COLLABORATORS = 15;
	
	private final String diagramId;
	
	/* Thread-safe Set to store the active WebSocket sessions */
	private final Set<WebSocketSession> sessions;
	
	private String lastSnapshot;

	public Room(String diagramId) {
		this.sessions = Collections.synchronizedSet(new HashSet<>());
		this.diagramId = diagramId;
		this.lastSnapshot = "";
	}

	@Override
	public void close() {
		sessions.forEach(session -> {
			if (session.isOpen()) {
				try {
					session.close();
					// Close the socket gracefully
				} catch (Exception e) {
					log.error("Error closing session {}: {}", session.getId(), e.getMessage());
				}
			}
		});
		sessions.clear();
	}

	/**
	 * Adds a session to the room, checking the capacity limit.
	 * 
	 * @param session The WebSocketSession to add.
	 * @throws CollaboratorsCapacityException if the limit is reached.
	 */
	@Override
	public void addSession(WebSocketSession session) {
		if (sessions.size() >= MAX_COLLABORATORS) {
			throw new CollaboratorsCapacityException(
					"Maximum number of Collaborators is " + MAX_COLLABORATORS + " for diagram " + diagramId);
		}
		sessions.add(session);
	}

	/**
	 * Removes a session from the room.
	 * 
	 * @param session The WebSocketSession to remove.
	 * @return true if the session was removed, false otherwise.
	 */
	@Override
	public boolean removeSession(WebSocketSession session) {
		return sessions.remove(session);
	}

	/**
	 * Checks if the room is empty.
	 * 
	 * @return true if there are no sessions, false otherwise.
	 */
	@Override
	public boolean isEmpty() {
		return sessions.isEmpty();
	}
}