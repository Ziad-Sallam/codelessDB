package backend.collab.snapshot;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import backend.collab.exceptions.CollabException.YDocUpdateException;
import backend.collab.services.RedisStreamService;
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

	public SnapshotDto getLatestDiagram(int userId, UUID diagramId) {
		userDiagramService.getUserOrThrow(userId);
		Role role = userDiagramService
						.getUserDiagramOrThrow(userId, diagramId)
						.getRole();

		takeSnapshotThread(diagramId.toString());
		Diagram diagram = userDiagramService.getDiagramOrThrow(diagramId);
		byte[] snapshot = diagram.getContent();

		return new SnapshotDto(snapshot, diagram.getName(), role);
	}

	public void takeSnapshot(String diagramId) {
		updateWriter.submitWriteTask(() -> takeSnapshotThread(diagramId));
	}

	private void takeSnapshotThread(String diagramId) {
		Diagram diagram = userDiagramService.getDiagramOrThrow(UUID.fromString(diagramId));

		// send a request to a Node.js worker node 
		// to take a snapshot from redis updates and deletes them
		byte[] snapshot = snapshotClient.snapshot(diagramId);
		log.info("Snapshot taken {}", snapshot);
		
		if (snapshot != null && snapshot.length > 0) {
			diagram.setContent(snapshot);
			diagramRepository.save(diagram);
		}
	}
}
