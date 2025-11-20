package backend.userDiagramManagment;

import backend.entities.Diagram;
import backend.entities.User;
import backend.entities.joins.UserDiagram;
import backend.user.Role;
import backend.user.UserRepository;
import backend.user.exceptions.UserException;
import backend.userDiagramManagement.dto.*;
import backend.userDiagramManagement.dto.create.DiagramCreateRequestDto;
import backend.userDiagramManagement.dto.search.DiagramSearchRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareRequestDto;
import backend.userDiagramManagement.dto.share.DiagramShareResponseDto;
import backend.userDiagramManagement.dto.update.DiagramUpdateRequestDto;
import backend.userDiagramManagement.exceptions.DiagramException;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.repository.UserDiagramRepository;
import backend.userDiagramManagement.service.UserDiagramService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.sql.Date;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserDiagramServiceTest {

    @Mock
    private DiagramRepository diagramRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDiagramRepository userDiagramRepository;

    @InjectMocks
    private UserDiagramService diagramService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ===== CREATE =====
    @Test
    void createDiagram_success() {
        User user = new User(); user.setId(1);
        when(userRepository.findById(1)).thenReturn(user);

        DiagramCreateRequestDto dto = new DiagramCreateRequestDto();
        dto.setName("Test Diagram");

        Diagram savedDiagram = new Diagram();
        savedDiagram.setId(UUID.randomUUID());
        savedDiagram.setLastModified(new Date(System.currentTimeMillis()));

        when(diagramRepository.save(any(Diagram.class))).thenReturn(savedDiagram);

        UUID diagramId = diagramService.createDiagram(1, dto);

        assertEquals(savedDiagram.getId(), diagramId);
        verify(userDiagramRepository, times(1)).save(any(UserDiagram.class));
    }

    @Test
    void createDiagram_userNotFound() {
        when(userRepository.findById(99)).thenReturn(null);
        DiagramCreateRequestDto dto = new DiagramCreateRequestDto();
        assertThrows(UserException.UserNotFoundException.class,
                () -> diagramService.createDiagram(99, dto));
    }

    // ===== UPDATE =====
    @Test
    void updateDiagram_success() {
        // Arrange
        Diagram diagram = new Diagram();
        diagram.setId(UUID.randomUUID());
        diagram.setName("Old");
        diagram.setContent("old");
        diagram.setLastModified(new Date(System.currentTimeMillis()));

        UserDiagram owner = new UserDiagram();
        owner.setRole(Role.OWNER);

        when(userRepository.existsById(1)).thenReturn(true);
        when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
        when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                .thenReturn(Optional.of(owner));

        // Mock saving to update lastModified
        when(diagramRepository.save(any(Diagram.class))).thenAnswer(invocation -> {
            Diagram d = invocation.getArgument(0);
            d.setLastModified(new Date(System.currentTimeMillis()));
            return d;
        });

        DiagramUpdateRequestDto dto = new DiagramUpdateRequestDto();
        dto.setId(diagram.getId());
        dto.setName("New");
        dto.setJsonContent("new");
        dto.setThumbnail("thumb".getBytes());

        // Act
        Date lastModified = diagramService.updateDiagram(1, dto);

        // Assert
        assertNotNull(lastModified, "lastModified should not be null");
        assertEquals("New", diagram.getName(), "Diagram name should be updated");
        assertEquals("new", diagram.getContent(), "Diagram content should be updated");
        assertArrayEquals("thumb".getBytes(), diagram.getThumbnail(), "Diagram thumbnail should be updated");
    }


    @Test
    void updateDiagram_permissionDenied() {
        Diagram diagram = new Diagram(); diagram.setId(UUID.randomUUID());
        UserDiagram userDiagram = new UserDiagram(); userDiagram.setRole(Role.WRITER);

        when(userRepository.existsById(1)).thenReturn(true);
        when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
        when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                .thenReturn(Optional.of(userDiagram));

        DiagramUpdateRequestDto dto = new DiagramUpdateRequestDto();
        dto.setId(diagram.getId());

        assertThrows(DiagramException.PermissionDeniedException.class,
                () -> diagramService.updateDiagram(1, dto));
    }

    // ===== DELETE =====
    @Test
    void deleteDiagram_success() {
        UUID id = UUID.randomUUID();
        Diagram diagram = new Diagram(); diagram.setId(id);
        UserDiagram owner = new UserDiagram(); owner.setRole(Role.OWNER);

        when(userRepository.existsById(1)).thenReturn(true);
        when(diagramRepository.findById(id)).thenReturn(Optional.of(diagram));
        when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, id))
                .thenReturn(Optional.of(owner));

        assertDoesNotThrow(() -> diagramService.deleteDiagram(1, id));
        verify(diagramRepository).delete(diagram);
    }

    @Test
    void deleteDiagram_permissionDenied() {
        UUID id = UUID.randomUUID();
        Diagram diagram = new Diagram(); diagram.setId(id);
        UserDiagram writer = new UserDiagram(); writer.setRole(Role.WRITER);

        when(userRepository.existsById(1)).thenReturn(true);
        when(diagramRepository.findById(id)).thenReturn(Optional.of(diagram));
        when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, id))
                .thenReturn(Optional.of(writer));

        assertThrows(DiagramException.PermissionDeniedException.class,
                () -> diagramService.deleteDiagram(1, id));
    }

    // ===== SEARCH BY ID =====
    @Test
    void searchDiagramById_success() {
        UUID id = UUID.randomUUID();
        Diagram diagram = new Diagram(); diagram.setId(id);
        UserDiagram owner = new UserDiagram(); owner.setRole(Role.OWNER);

        when(userRepository.existsById(1)).thenReturn(true);
        when(diagramRepository.findById(id)).thenReturn(Optional.of(diagram));
        when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, id))
                .thenReturn(Optional.of(owner));

        DiagramDto dto = diagramService.searchDiagramById(1, id);
        assertEquals(Role.OWNER, Role.valueOf(dto.getRole()));
    }

    @Test
    void searchDiagramById_permissionDenied() {
        UUID id = UUID.randomUUID();
        Diagram diagram = new Diagram(); diagram.setId(id);

        when(userRepository.existsById(1)).thenReturn(true);
        when(diagramRepository.findById(id)).thenReturn(Optional.of(diagram));
        when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, id)).thenReturn(Optional.empty());

        assertThrows(DiagramException.PermissionDeniedException.class,
                () -> diagramService.searchDiagramById(1, id));
    }

    // ===== SEARCH PAGINATED =====
    @Test
    void searchDiagrams_success() {
        DiagramSearchRequestDto request = new DiagramSearchRequestDto();
        request.setName("test");

        Diagram diagram = new Diagram(); diagram.setId(UUID.randomUUID());
        UserDiagram ud = new UserDiagram(); ud.setDiagram(diagram); ud.setRole(Role.OWNER);

        Page<UserDiagram> page = new PageImpl<>(List.of(ud));

        when(userRepository.existsById(1)).thenReturn(true);
        when(userDiagramRepository.findAllByUser_IdAndDiagram_NameContainingIgnoreCaseAndDiagram_CreatedAtBetween(
                anyInt(), anyString(), any(), any(), any(Pageable.class)
        )).thenReturn(page);

        Page<DiagramDto> result = diagramService.searchDiagrams(1, request, Pageable.unpaged());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchDiagrams_invalidDate() {
        when(userRepository.existsById(1)).thenReturn(true);

        DiagramSearchRequestDto request = new DiagramSearchRequestDto();
        request.setStart("invalid-date");
        request.setEnd("2025-01-01");

        assertThrows(DiagramException.InvalidDiagramDataException.class,
                () -> diagramService.searchDiagrams(1, request, Pageable.unpaged()));
    }


    // ===== SHARE =====
    @Test
    void shareDiagram_success() {
        UUID id = UUID.randomUUID();
        Diagram diagram = new Diagram(); diagram.setId(id);
        UserDiagram owner = new UserDiagram(); owner.setRole(Role.OWNER);
        User target = new User(); target.setId(2); target.setUsername("alice");

        when(userRepository.existsById(1)).thenReturn(true);
        when(diagramRepository.findById(id)).thenReturn(Optional.of(diagram));
        when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, id)).thenReturn(Optional.of(owner));
        when(userRepository.findByUsername("alice")).thenReturn(target);
        when(userDiagramRepository.existsByUser_IdAndDiagram_Id(2, id)).thenReturn(false);

        DiagramShareRequestDto request = new DiagramShareRequestDto();
        request.setDiagramId(id); request.setToUserName("alice"); request.setRole("OWNER");

        DiagramShareResponseDto response = diagramService.shareDiagram(1, request);
        assertEquals("alice", response.getSharedWith());
        assertEquals("OWNER", response.getRole());
    }

    @Test
    void shareDiagram_alreadyShared() {
        UUID id = UUID.randomUUID();
        Diagram diagram = new Diagram(); diagram.setId(id);
        UserDiagram owner = new UserDiagram(); owner.setRole(Role.OWNER);
        User target = new User(); target.setId(2); target.setUsername("alice");

        when(userRepository.existsById(1)).thenReturn(true);
        when(diagramRepository.findById(id)).thenReturn(Optional.of(diagram));
        when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, id)).thenReturn(Optional.of(owner));
        when(userRepository.findByUsername("alice")).thenReturn(target);
        when(userDiagramRepository.existsByUser_IdAndDiagram_Id(2, id)).thenReturn(true);

        DiagramShareRequestDto request = new DiagramShareRequestDto();
        request.setDiagramId(id); request.setToUserName("alice"); request.setRole("OWNER");

        assertThrows(DiagramException.InvalidDiagramDataException.class,
                () -> diagramService.shareDiagram(1, request));
    }

    @Test
    void shareDiagram_permissionDenied() {
        UUID id = UUID.randomUUID();
        Diagram diagram = new Diagram(); diagram.setId(id);
        UserDiagram writer = new UserDiagram(); writer.setRole(Role.WRITER);

        when(userRepository.existsById(1)).thenReturn(true);
        when(diagramRepository.findById(id)).thenReturn(Optional.of(diagram));
        when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, id)).thenReturn(Optional.of(writer));

        DiagramShareRequestDto request = new DiagramShareRequestDto();
        request.setDiagramId(id); request.setToUserName("alice"); request.setRole("OWNER");

        assertThrows(DiagramException.PermissionDeniedException.class,
                () -> diagramService.shareDiagram(1, request));
    }

    @Test
    void shareDiagram_targetUserNotFound() {
        UUID id = UUID.randomUUID();
        Diagram diagram = new Diagram(); diagram.setId(id);
        UserDiagram owner = new UserDiagram(); owner.setRole(Role.OWNER);

        when(userRepository.existsById(1)).thenReturn(true);
        when(diagramRepository.findById(id)).thenReturn(Optional.of(diagram));
        when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, id)).thenReturn(Optional.of(owner));
        when(userRepository.findByUsername("alice")).thenReturn(null);

        DiagramShareRequestDto request = new DiagramShareRequestDto();
        request.setDiagramId(id); request.setToUserName("alice"); request.setRole("OWNER");

        assertThrows(UserException.UserNotFoundException.class,
                () -> diagramService.shareDiagram(1, request));
    }

}
