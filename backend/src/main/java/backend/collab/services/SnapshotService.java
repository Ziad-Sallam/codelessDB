package backend.collab.services;

import java.sql.Date;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.collab.update.DiagramUpdateRequestDto;
import backend.entities.Diagram;
import backend.entities.User;
import backend.entities.joins.UserDiagram;
import backend.user.Role;
import backend.user.UserRepository;
import backend.user.exceptions.UserException;
import backend.userDiagramManagement.dto.DiagramDto;
import backend.userDiagramManagement.exceptions.DiagramException;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.repository.UserDiagramRepository;
import backend.userDiagramManagement.service.UserDiagramService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SnapshotService {
	private final DiagramRepository diagramRepository;
	private final UserRepository userRepository;
	private final UserDiagramService userDiagramService;

	@Transactional
	public Date updateDiagram(int userId, DiagramUpdateRequestDto request, UUID diagramId) {
		userDiagramService.getUserOrThrow(userId);
		Diagram diagram = userDiagramService.getDiagramOrThrow(diagramId);
		UserDiagram userDiagram = userDiagramService.getUserDiagramOrThrow(userId, diagramId);

		if (userDiagram.getRole() == Role.READER) {
			throw new DiagramException.PermissionDeniedException("Only the owner can update this diagram");
		}

		if (request.getName() != null) {
			diagram.setName(request.getName());
		}
		if (request.getJsonContent() != null) {
			diagram.setContent(request.getJsonContent());
		}
		if (request.getThumbnail() != null) {
			diagram.setThumbnail(request.getThumbnail());
		}
		return diagramRepository.save(diagram).getLastModified();
	}

	public DiagramDto searchDiagramById(int userId, UUID diagramId) {
		userDiagramService.getUserOrThrow(userId);
		Diagram diagram = userDiagramService.getDiagramOrThrow(diagramId);
		UserDiagram userDiagram = userDiagramService.getUserDiagramOrThrow(userId, diagramId);
		return DiagramDto.toDto(diagram, userDiagram.getRole());
	}

}
