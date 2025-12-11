package backend.collab.snapshot;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.collab.services.RedisStreamService;
import backend.entities.Diagram;
import backend.entities.User;
import backend.entities.joins.UserDiagram;
import backend.user.Role;
import backend.userDiagramManagement.dto.DiagramDto;
import backend.userDiagramManagement.dto.update.DiagramUpdateRequestDto;
import backend.userDiagramManagement.exceptions.DiagramException;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.service.UserDiagramService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SnapshotService {
	
	private final DiagramRepository diagramRepository;
	
	private final DiagramPendingUpdateRepository updatesRepository;
	
	private final UserDiagramService userDiagramService;
	
	private final RedisStreamService redisService;

	public SnapshotDto getLatestDiagram(int userId, UUID diagramId) {
		userDiagramService.getUserOrThrow(userId);
		Role role = userDiagramService.getUserDiagramOrThrow(userId, diagramId).getRole();
		
		Diagram diagram = userDiagramService.getDiagramOrThrow(diagramId);
		byte[] snapshot = diagram.getContent();
		List<byte[]> updates = updatesRepository.findAllUpdateDataByDiagramId(diagramId.toString());
		return new SnapshotDto(snapshot, updates, diagram.getName(), role);
	}
	
	public void takeSnapshot(
			int userId, 
			UUID diagramId, 
			byte[] state, 
			String newDiagramName, 
			String picture
	) {

		userDiagramService.getUserOrThrow(userId);
		userDiagramService.getUserDiagramOrThrow(userId, diagramId);
		
		Diagram diagram = userDiagramService.getDiagramOrThrow(diagramId);
		diagram.setContent(state);
		diagram.setName(newDiagramName);
		diagram.setThumbnail(picture);
		diagramRepository.save(diagram);

		String id = diagramId.toString();
		redisService.removeDiagramHistory(id);
		updatesRepository.deleteAllByDiagramId(id);
	}

}
