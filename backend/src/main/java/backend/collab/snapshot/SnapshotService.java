package backend.collab.snapshot;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import at.yrs4j.wrapper.interfaces.YDoc;
import at.yrs4j.wrapper.interfaces.YTransaction;
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

	private final RedisStreamService redisService;

	private final UpdateWriter updateWriter;

	public SnapshotDto getLatestDiagram(int userId, UUID diagramId) {
		userDiagramService.getUserOrThrow(userId);
		Role role = userDiagramService.getUserDiagramOrThrow(userId, diagramId).getRole();
		
		Diagram diagram = userDiagramService.getDiagramOrThrow(diagramId);
		byte[] snapshot = diagram.getContent();
		
		return new SnapshotDto(snapshot, diagram.getName(), role);
	}

	/**
	 * Move all updates from redis and apply it on the old YDoc snapshot <br>
	 * Then save the latest snapshot from YDoc to the DB
	 * 
	 * @param diagramId
	 */
	public void takeSnapshot(YDoc document, String diagramId) {
		updateWriter.submitWriteTask(() -> {
			takeSnapshotThread(document, diagramId);
		});
	}

	private void takeSnapshotThread(YDoc document, String diagramId) {
		Diagram diagram = userDiagramService.getDiagramOrThrow(UUID.fromString(diagramId));

		List<byte[]> redisUpdates = redisService.getAllUpdates(diagramId);
		log.info("redis has {} updates", redisUpdates.size());
		if (redisUpdates.isEmpty()) {
			return; // nothing to snapshot
		}

		byte[] snapshot;

		// lock per diagram and apply all pending updates
		synchronized (document) {
			YTransaction txn = document.writeTransaction();
			for (byte[] update : redisUpdates) {
				byte err = txn.apply(update);
				if (err != 0) {
					throw new YDocUpdateException("Invalid Yrs update for diagram " + diagramId);
				}
			}

			snapshot = txn.stateDiffV1(new byte[] { 0 });
			// txn.commit();
		}

		diagram.setContent(snapshot);
		diagramRepository.save(diagram);

		redisService.removeDiagramHistory(diagramId);
	}

}
