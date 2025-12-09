package backend.collab;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import backend.collab.services.SnapshotService;
import backend.collab.updateDto.DiagramUpdateRequestDto;
import backend.entities.Diagram;
import backend.entities.User;
import backend.entities.joins.UserDiagram;
import backend.entities.joins.UserDiagramId;
import backend.user.Role;
import backend.user.UserRepository;
import backend.userDiagramManagement.dto.DiagramDto;
import backend.userDiagramManagement.exceptions.DiagramException;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.repository.UserDiagramRepository;

public class CollabTest {

	@Mock
	private DiagramRepository diagramRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private UserDiagramRepository userDiagramRepository;

	@InjectMocks
	private SnapshotService service;

	private User user;
	private Diagram diagram;
	private UserDiagram ownerLink;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);

		user = new User();
		user.setId(1);
		user.setUsername("john");
		user.setPicture("john.png");

		diagram = Diagram.builder()
				.id(UUID.randomUUID())
				.name("Test Diagram")
				.content(new byte[0])
				.thumbnail("thumb.png")
				.lastModified(new Date(System.currentTimeMillis()))
				.createdAt(new Date(System.currentTimeMillis()))
				.build();

		ownerLink = UserDiagram.builder()
				.UUID(new UserDiagramId(1, diagram.getId()))
				.user(user)
				.diagram(diagram)
				.role(Role.OWNER)
				.build();
	}

	// ------------------------------------------------------------
	// UPDATE DIAGRAM
	// ------------------------------------------------------------
	@Test
	void updateDiagram_success() {
		DiagramUpdateRequestDto request = new DiagramUpdateRequestDto();
		request.setName("Updated");
		request.setJsonContent(new byte[0]);

		when(userRepository.findById(1)).thenReturn(user);
		when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
		when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
				.thenReturn(Optional.of(ownerLink));
		when(diagramRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		Date updated = service.updateDiagram(1, request, diagram.getId());

		assertNotNull(updated);
		assertEquals("Updated", diagram.getName());
		assertEquals("{updated}", diagram.getContent());
	}

	@Test
	void updateDiagram_forbiddenForReader() {
		ownerLink.setRole(Role.READER);

		when(userRepository.findById(1)).thenReturn(user);
		when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
		when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
				.thenReturn(Optional.of(ownerLink));

		assertThrows(DiagramException.PermissionDeniedException.class,
				() -> service.updateDiagram(1, new DiagramUpdateRequestDto(), diagram.getId()));
	}

	// ------------------------------------------------------------
	// SEARCH BY ID
	// ------------------------------------------------------------
	@Test
	void searchById_success() {
		when(userRepository.findById(1)).thenReturn(user);
		when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
		when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
				.thenReturn(Optional.of(ownerLink));

		DiagramDto dto = service.searchDiagramById(1, diagram.getId());

		assertEquals(diagram.getId(), dto.getId());
		assertEquals("Test Diagram", dto.getName());
		assertEquals("{json}", dto.getContent());
		assertEquals(Role.OWNER, dto.getRole());
	}

}