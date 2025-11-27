package backend.userDiagramManagment;

import backend.entities.Diagram;
import backend.entities.User;
import backend.entities.joins.UserDiagram;
import backend.entities.joins.UserDiagramId;
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
import org.springframework.data.domain.*;

import java.sql.Date;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserDiagramServiceTest {

    @Mock
    private DiagramRepository diagramRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDiagramRepository userDiagramRepository;

    @InjectMocks
    private UserDiagramService service;

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
                .content("{json}")
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
    // GET DIAGRAMS BY USER
    // ------------------------------------------------------------
    @Test
    void getDiagramsByUserId_success() {
        when(userRepository.findById(1)).thenReturn(user);
        when(userDiagramRepository.findByUser_Id(eq(1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ownerLink)));

        Page<DiagramInfoDto> result = service.getDiagramsByUserId(1, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        DiagramInfoDto dto = result.getContent().get(0);
        assertEquals(diagram.getId(), dto.getDiagramId());
        assertEquals("Test Diagram", dto.getName());
        assertEquals("thumb.png", dto.getThumbnail());
        assertEquals(Role.OWNER, dto.getRole());
    }

    // ------------------------------------------------------------
    // CREATE DIAGRAM
    // ------------------------------------------------------------
    @Test
    void createDiagram_success() {
        when(userRepository.findById(1)).thenReturn(user);
        when(diagramRepository.save(any(Diagram.class))).thenReturn(diagram);
        when(userDiagramRepository.save(any(UserDiagram.class))).thenReturn(ownerLink);
        when(userDiagramRepository.findByDiagram_Id(diagram.getId())).thenReturn(List.of(ownerLink));

        DiagramCreateRequestDto request = new DiagramCreateRequestDto();
        request.setThumbnail("thumb.png");

        DiagramInfoDto result = service.createDiagram(1, request);

        assertNotNull(result.getDiagramId());
        assertEquals("Test Diagram", result.getName());
        verify(userDiagramRepository, times(1)).save(any(UserDiagram.class));
    }

    @Test
    void createDiagram_userNotFound() {
        when(userRepository.findById(1)).thenReturn(null);

        assertThrows(UserException.UserNotFoundException.class,
                () -> service.createDiagram(1, new DiagramCreateRequestDto()));
    }

    // ------------------------------------------------------------
    // UPDATE DIAGRAM
    // ------------------------------------------------------------
    @Test
    void updateDiagram_success() {
        DiagramUpdateRequestDto request = new DiagramUpdateRequestDto();
        request.setName("Updated");
        request.setJsonContent("{updated}");

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
    // DELETE DIAGRAM
    // ------------------------------------------------------------
    @Test
    void deleteDiagram_lastUser_deletesDiagram() {
        when(userRepository.findById(1)).thenReturn(user);
        when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                .thenReturn(Optional.of(ownerLink));
        // simulate no other users after deletion
        when(userDiagramRepository.findFirstByDiagram_Id(diagram.getId())).thenReturn(null);
        when(userDiagramRepository.existsByDiagram_Id(diagram.getId())).thenReturn(false);
        when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));

        service.deleteDiagram(1, diagram.getId());

        verify(userDiagramRepository).delete(ownerLink);
        verify(diagramRepository).delete(diagram);
    }

    @Test
    void deleteDiagram_hasOtherUsers_notDeleteDiagram() {
        when(userRepository.findById(1)).thenReturn(user);
        when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                .thenReturn(Optional.of(ownerLink));
        // simulate someone else still exists
        when(userDiagramRepository.existsByDiagram_Id(diagram.getId())).thenReturn(true);

        service.deleteDiagram(1, diagram.getId());

        verify(userDiagramRepository).delete(ownerLink);
        verify(diagramRepository, never()).delete(any());
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

    // ------------------------------------------------------------
    // SEARCH WITH FILTER
    // ------------------------------------------------------------
    @Test
    void searchDiagrams_success() {
        when(userRepository.findById(1)).thenReturn(user);

        // when searching, repository returns a page with one UserDiagram
        when(userDiagramRepository
                .findAllByUser_IdAndDiagram_NameContainingIgnoreCaseAndDiagram_CreatedAtBetween(
                        eq(1), anyString(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ownerLink)));

        // getContributors uses findByDiagram_Id
        when(userDiagramRepository.findByDiagram_Id(diagram.getId()))
                .thenReturn(List.of(ownerLink));

        DiagramSearchRequestDto request = new DiagramSearchRequestDto();
        Page<DiagramInfoDto> result = service.searchDiagrams(1, request, Pageable.unpaged());

        assertEquals(1, result.getContent().size());
        DiagramInfoDto dto = result.getContent().get(0);
        assertEquals(diagram.getId(), dto.getDiagramId());
        assertEquals("Test Diagram", dto.getName());
        assertEquals("thumb.png", dto.getThumbnail());
        assertEquals(Role.OWNER, dto.getRole());
        assertEquals(1, dto.getContributors().size());
        assertEquals("john", dto.getContributors().get(0).name());
    }

    // ------------------------------------------------------------
    // SHARE DIAGRAM
    // ------------------------------------------------------------
    @Test
    void shareDiagram_success() {
        User target = new User();
        target.setId(2);
        target.setUsername("mike");
        target.setPicture("mike.png");

        DiagramShareRequestDto req = new DiagramShareRequestDto();
        req.setToUserName("mike");
        req.setRole(Role.WRITER);
        req.setDelete(false);

        when(userRepository.findById(1)).thenReturn(user);
        when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
        when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                .thenReturn(Optional.of(ownerLink));
        when(userRepository.findByUsername("mike")).thenReturn(target);
        when(userDiagramRepository.findByUser_IdAndDiagram_Id(2, diagram.getId()))
                .thenReturn(Optional.empty());
        when(userDiagramRepository.save(any(UserDiagram.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DiagramShareResponseDto res = service.shareDiagram(1, diagram.getId(), req);

        assertEquals("Diagram shared successfully", res.getMessage());
        assertEquals("mike", res.getSharedWith());
        assertEquals(Role.WRITER, res.getRole());
    }

    @Test
    void shareDiagram_targetAlreadyHasAccess_sameRole_throws() {
        DiagramShareRequestDto req = new DiagramShareRequestDto();
        req.setToUserName("mike");
        req.setRole(Role.WRITER);
        req.setDelete(false);

        User target = new User();
        target.setId(2);
        target.setUsername("mike");

        UserDiagram existing = UserDiagram.builder()
                .UUID(new UserDiagramId(2, diagram.getId()))
                .user(target)
                .diagram(diagram)
                .role(Role.WRITER)
                .build();

        when(userRepository.findById(1)).thenReturn(user);
        when(diagramRepository.findById(diagram.getId())).thenReturn(Optional.of(diagram));
        when(userDiagramRepository.findByUser_IdAndDiagram_Id(1, diagram.getId()))
                .thenReturn(Optional.of(ownerLink));
        when(userRepository.findByUsername("mike")).thenReturn(target);
        when(userDiagramRepository.findByUser_IdAndDiagram_Id(2, diagram.getId()))
                .thenReturn(Optional.of(existing));

        assertThrows(DiagramException.InvalidDiagramDataException.class,
                () -> service.shareDiagram(1, diagram.getId(), req));
    }
}
