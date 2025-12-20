package backend.collab.snapshot;

import java.util.UUID;

import org.springframework.stereotype.Service;

import backend.collab.services.UpdateWriter;
import backend.entities.Diagram;
import backend.user.Role;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.service.UserDiagramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnapshotService {

	private final DiagramRepository diagramRepository;

	private final UserDiagramService userDiagramService;

	private final YjsSnapshotClient snapshotClient;

	private final UpdateWriter updateWriter;

	public SnapshotDto getDiagramMetadata(int userId, UUID diagramId) {
		userDiagramService.getUserOrThrow(userId);
		Role role = userDiagramService
				.getUserDiagramOrThrow(userId, diagramId)
				.getRole();

		Diagram diagram = userDiagramService.getDiagramOrThrow(diagramId);
		// Return DTO with null snapshot data, only metadata
		return new SnapshotDto(null, diagram.getName(), role);
	}

	public byte[] getDiagramSnapshot(int userId, UUID diagramId) {
		userDiagramService.getUserOrThrow(userId);
		// Access check
		userDiagramService.getUserDiagramOrThrow(userId, diagramId);

		takeSnapshotThread(diagramId.toString());
		Diagram diagram = userDiagramService.getDiagramOrThrow(diagramId);
		return diagram.getContent();
	}

	public void takeSnapshot(String diagramId) {
		updateWriter.submitWriteTask(() -> takeSnapshotThread(diagramId));
	}

	private final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.locks.Lock> snapshotLocks = new java.util.concurrent.ConcurrentHashMap<>();

	private void takeSnapshotThread(String diagramId) {
		java.util.concurrent.locks.Lock lock = snapshotLocks.computeIfAbsent(diagramId,
				k -> new java.util.concurrent.locks.ReentrantLock());
		lock.lock();
		try {
			Diagram diagram = userDiagramService.getDiagramOrThrow(UUID.fromString(diagramId));

			// send a request to a Node.js worker node
			// to take a snapshot from redis updates and deletes them
			byte[] snapshot = snapshotClient.snapshot(diagramId, diagram.getContent());
			log.info("Snapshot taken {}", snapshot);

			if (snapshot != null && snapshot.length > 0) {
				diagram.setContent(snapshot);
				diagramRepository.save(diagram);
			}
		} finally {
			lock.unlock();
		}
	}
}
