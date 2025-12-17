package backend.publicDiagramManagement.PublucDiagramServiceTest;

import backend.entities.Diagram;
import backend.entities.joins.UserDiagram;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.dto.PublicDiagramInfoDto;
import backend.publicDiagramManagement.exceptions.PublicDiagramException;
import backend.publicDiagramManagement.repository.ForkRepository;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.publicDiagramManagement.service.PublicDiagramServiceImpl;
import backend.user.Role;
import backend.user.UserService;
import backend.userDiagramManagement.dto.ContributorDto;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.repository.UserDiagramRepository;
import backend.userDiagramManagement.service.UserDiagramService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ForkTests {

    @Mock
    private UserDiagramService userDiagramService;
    @Mock
    private UserDiagramRepository userDiagramRepository;
    @Mock
    private DiagramRepository diagramRepository;
    @Mock
    private UserService userService;
    @Mock
    private PublicDiagramRepository publicDiagramRepository;
    @Mock
    private ForkRepository forkRepository;

    @InjectMocks
    private PublicDiagramServiceImpl service;

    private UUID diagramId;
    private Diagram originalDiagram;
    private PublicDiagram publicDiagram;

    @BeforeEach
    void setup() {
        diagramId = UUID.randomUUID();

        originalDiagram = Diagram.builder()
                .id(diagramId)
                .name("Original")
                .ddl("DDL")
                .build();

        publicDiagram = new PublicDiagram();
        publicDiagram.setId(diagramId);
        publicDiagram.setDiagram(originalDiagram);
        publicDiagram.setForks(0);

        originalDiagram.setPublicDiagram(publicDiagram);
    }

    @Test
    void fork_success_createsCloneAndRecord() {

        when(userDiagramService.getDiagramOrThrow(diagramId))
                .thenReturn(originalDiagram);

        when(userService.getUserOrThrow(5))
                .thenReturn(null);

        service.forkPublicDiagram(5, diagramId);

        // clone saved
        verify(diagramRepository).save(any(Diagram.class));

        // owner UserDiagram saved
        verify(userDiagramRepository).save(any(UserDiagram.class));

        // fork record saved
        verify(forkRepository).save(any());

        // fork count incremented
        verify(publicDiagramRepository).incrementForks(diagramId);
    }

    @Test
    void getForkedDiagrams_returnsPublicDiagramInfoDto() {

        UUID forkedId = UUID.randomUUID();

        // Build public diagram (includes Diagram inside)
        Diagram diagram = Diagram.builder()
                .id(forkedId)
                .name("Forked")
                .ddl("DDL")
                .build();

        PublicDiagram publicDiagram = PublicDiagram.builder()
                .id(forkedId)
                .diagram(diagram)
                .shortDescription("Short")
                .stars(5)
                .views(10)
                .forks(2)
                .build();

        Page<PublicDiagram> page = new PageImpl<>(
                List.of(publicDiagram),
                PageRequest.of(0, 10),
                1);

        // ✅ new repository call returns PublicDiagram
        when(forkRepository.findForkedPublicDiagramsByUser(7, PageRequest.of(0, 10)))
                .thenReturn(page);

        // contributors
        List<ContributorDto> contributors = List.of(
                new ContributorDto("Alice", "pic", Role.OWNER));

        when(userDiagramService.getContributors(forkedId))
                .thenReturn(contributors);

        // ✅ service now returns PublicDiagramInfoDto
        Page<PublicDiagramInfoDto> result = service.getForkedPublicDiagrams(7, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());

        PublicDiagramInfoDto dto = result.getContent().get(0);

        // base diagram fields
        assertEquals("Forked", dto.getName());
        assertEquals(forkedId, dto.getDiagramId());

        // contributors
        assertEquals(1, dto.getContributors().size());
        assertEquals("Alice", dto.getContributors().get(0).getName());

        // public diagram fields
        assertEquals(5, dto.getStars());
        assertEquals(10, dto.getViews());
        assertEquals(2, dto.getForks());
        assertEquals("Short", dto.getShortDescription());

        // verify interactions
        verify(forkRepository).findForkedPublicDiagramsByUser(7, PageRequest.of(0, 10));
        verify(userDiagramService).getContributors(forkedId);
    }

    @Test
    void fork_missingDiagram_throws() {

        when(userDiagramService.getDiagramOrThrow(diagramId))
                .thenThrow(new PublicDiagramException.DiagramNotFoundException("not found"));

        assertThrows(PublicDiagramException.DiagramNotFoundException.class,
                () -> service.forkPublicDiagram(1, diagramId));
    }

    @Test
    void fork_notPublicDiagram_throws() {

        originalDiagram.setPublicDiagram(null);

        when(userDiagramService.getDiagramOrThrow(diagramId))
                .thenReturn(originalDiagram);

        assertThrows(PublicDiagramException.DiagramNotFoundException.class,
                () -> service.forkPublicDiagram(2, diagramId));
    }
}
