package backend.collab.snapshot;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import backend.collab.services.UpdateWriter;
import backend.entities.Diagram;
import backend.user.Role;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.service.UserDiagramService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SnapshotService {

	private final DiagramRepository diagramRepository;

	private final UserDiagramService userDiagramService;

	private final YjsSnapshotClient snapshotClient;

	private final UpdateWriter updateWriter;

	private final ConcurrentHashMap<String, Lock> snapshotLocks = new ConcurrentHashMap<>();

	public SnapshotDto getDiagramMetadata(int userId, UUID diagramId) {
		userDiagramService.getUserOrThrow(userId);
		Role role = userDiagramService
						.getUserDiagramOrThrow(userId, diagramId)
						.getRole();

		Diagram diagram = userDiagramService.getDiagramOrThrow(diagramId);
		
		return new SnapshotDto(diagram.getName(), role);
	}

	public byte[] getDiagramSnapshot(int userId, UUID diagramId) {
		userDiagramService.getUserOrThrow(userId);
		// Access check
		userDiagramService.getUserDiagramOrThrow(userId, diagramId);

		Diagram diagram = userDiagramService.getDiagramOrThrow(diagramId);
		return diagram.getContent();
	}

	public void takeSnapshot(String diagramId) {
		updateWriter.submitWriteTask(() -> takeSnapshotThread(diagramId));
	}

	public void takeSnapshotThread(String diagramId) {
		Lock lock = snapshotLocks.computeIfAbsent(diagramId, k -> new ReentrantLock());
		lock.lock();
		try {
			Diagram diagram = userDiagramService.getDiagramOrThrow(UUID.fromString(diagramId));

			// send a request to a Node.js worker node
			// to take a snapshot from redis updates and deletes them
			byte[] snapshot = snapshotClient.snapshot(diagramId, diagram.getContent());

			if (snapshot != null && snapshot.length > 0) {
				diagram.setContent(snapshot);
				diagramRepository.save(diagram);
			}

		} finally {
			lock.unlock();
		}
	}
}
