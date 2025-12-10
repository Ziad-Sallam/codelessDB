package backend.publicDiagramManagement;

import backend.entities.Diagram;
import backend.entities.joins.UserDiagram;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.exceptions.PublicDiagramException;
import backend.publicDiagramManagement.repository.ForkRepository;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.publicDiagramManagement.service.PublicDiagramServiceImpl;
import backend.user.Role;
import backend.user.UserService;
import backend.userDiagramManagement.dto.ContributorDto;
import backend.userDiagramManagement.dto.DiagramInfoDto;
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

    @Mock private UserDiagramService userDiagramService;
    @Mock private UserDiagramRepository userDiagramRepository;
    @Mock private DiagramRepository diagramRepository;
    @Mock private UserService userService;
    @Mock private PublicDiagramRepository publicDiagramRepository;
    @Mock private ForkRepository forkRepository;

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

    // ----------------------------------------------------------
    // ✅ 1. Successful fork: clone + record + increment
    // ----------------------------------------------------------
    @Test
    void fork_success_createsCloneAndRecord() {

        when(userDiagramService.getDiagramOrThrow(diagramId))
                .thenReturn(originalDiagram);

        when(userService.getUserOrThrow(5))
                .thenReturn(null);  // not used in identity

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

    // ----------------------------------------------------------
    // ✅ 2. Fetch forked diagrams → mapped to DiagramInfoDto
    // ----------------------------------------------------------
    @Test
    void getForkedDiagrams_returnsDiagramInfoDto() {

        UUID forkedId = UUID.randomUUID();
        Diagram forkDiagram = Diagram.builder()
                .id(forkedId)
                .name("Forked")
                .ddl("DDL")
                .build();

        Page<Diagram> page = new PageImpl<>(
                List.of(forkDiagram),
                PageRequest.of(0, 10),
                1
        );

        when(forkRepository.findForkedDiagramsByUser(7, PageRequest.of(0,10)))
                .thenReturn(page);

        List<ContributorDto> contributors = List.of(
                new ContributorDto("Alice", "pic", Role.OWNER)
        );

        when(userDiagramService.getContributors(forkedId))
                .thenReturn(contributors);

        Page<DiagramInfoDto> result =
                service.getForkedPublicDiagrams(7, PageRequest.of(0,10));

        assertEquals(1, result.getTotalElements());

        DiagramInfoDto dto = result.getContent().get(0);

        assertEquals("Forked", dto.getName());
        assertEquals(Role.OWNER, dto.getRole());
        assertEquals(1, dto.getContributorDtos().size());
        assertEquals("Alice", dto.getContributorDtos().get(0).getName());

        verify(forkRepository).findForkedDiagramsByUser(7, PageRequest.of(0,10));
        verify(userDiagramService).getContributors(forkedId);
    }

    // ----------------------------------------------------------
    // ✅ 3. Forking missing diagram → throws
    // ----------------------------------------------------------
    @Test
    void fork_missingDiagram_throws() {

        when(userDiagramService.getDiagramOrThrow(diagramId))
                .thenThrow(new PublicDiagramException.DiagramNotFoundException("not found"));

        assertThrows(PublicDiagramException.DiagramNotFoundException.class,
                () -> service.forkPublicDiagram(1, diagramId));
    }

    // ----------------------------------------------------------
    // ✅ 4. Diagram exists but not public → throws
    // ----------------------------------------------------------
    @Test
    void fork_notPublicDiagram_throws() {

        originalDiagram.setPublicDiagram(null);

        when(userDiagramService.getDiagramOrThrow(diagramId))
                .thenReturn(originalDiagram);

        assertThrows(PublicDiagramException.DiagramNotFoundException.class,
                () -> service.forkPublicDiagram(2, diagramId));
    }
}
