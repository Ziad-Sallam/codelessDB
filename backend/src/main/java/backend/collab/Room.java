package backend.collab;

import java.io.Closeable;
import java.io.IOException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import backend.collab.exceptions.CollabException.CollaboratorsCapacityException;
import backend.collab.snapshot.SnapshotService;
import backend.user.Role;

import lombok.extern.slf4j.Slf4j;

interface IRoom extends Closeable {

	void addSession(WebSocketSession session);

	boolean removeSession(WebSocketSession session);

	boolean isEmpty();

	void doUpdate(byte[] update, String senderId, Role role);

	void takeSnapshot();
}

@Slf4j
public class Room implements IRoom {

	private static final int MAX_COLLABORATORS = 15;

	/**
	 * Maximum number of updates done before taking
	 * a snapshot and saving it in the DB
	 */
	private static final int SNAPSHOT_THRESHOLD = 300;

	private final String diagramId;
	
	/* Thread-safe Set to store the active WebSocket sessions */
	private final Set<WebSocketSession> sessions;
	
	private final LongAdder updateCounter;
	
	private final SnapshotService snapshotService;


	public Room(String diagramId, SnapshotService snapshotService) {
		this.diagramId = diagramId;
		this.sessions = Collections.synchronizedSet(new HashSet<>());
		this.updateCounter = new LongAdder();
		
		this.snapshotService = snapshotService;
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
		boolean exist = sessions.remove(session);
		return exist;
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

	@Override
	public void doUpdate(byte[] update, String senderId, Role role) {

		if (this.sessions == null || this.sessions.isEmpty()) {
			log.warn("No sessions found for diagramId: {}", diagramId);
			return;
		}

		if (role == Role.READER) {
			log.warn("Readers cannot send updates");
			return;
		}

		final boolean cursorUpdate = (update[0] == 1);

		if (!cursorUpdate) {
			this.updateCounter.increment();
			if (updateCounter.sum() >= SNAPSHOT_THRESHOLD) {
				takeSnapshot();
				updateCounter.reset();
			}
		}

		sendUpdatesToUsers(update, senderId);
	}

	private void sendUpdatesToUsers(byte[] update, String senderId) {
		BinaryMessage message = new BinaryMessage(update);

		// Stream and send to the targeted room sessions
		this.sessions.parallelStream().forEach(session -> {
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

	@Override
	public void close() {
		this.sessions.forEach(session -> {
			if (session.isOpen()) {
				try {
					session.close(CloseStatus.NORMAL);

				} catch (Exception e) {
					log.error("Error closing session {}: {}", session.getId(), e.getMessage());
				}
			}
		});

		this.sessions.clear();
		this.updateCounter.reset();
	}

	@Override
	public void takeSnapshot() {
		snapshotService.takeSnapshot(this.diagramId);
	}
}