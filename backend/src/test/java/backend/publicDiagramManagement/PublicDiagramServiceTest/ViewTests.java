package backend.publicDiagramManagement.PublicDiagramServiceTest;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.entities.Diagram;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.dto.PublicDiagramDto;
import backend.publicDiagramManagement.exceptions.PublicDiagramException;
import backend.publicDiagramManagement.repository.StarRepository;
import backend.publicDiagramManagement.service.PublicDiagramServiceImpl;
import backend.publicDiagramManagement.service.ViewsService;
import backend.userDiagramManagement.dto.ContributorDto;
import backend.userDiagramManagement.service.UserDiagramService;

@ExtendWith(MockitoExtension.class)
class ViewTests {

    // ------------------------------------------------------------
    // Mock ALL dependencies (constructor injection!)
    // ------------------------------------------------------------
    @Mock
    private UserDiagramService userDiagramService;
    @Mock
    private ViewsService viewsService;
    @Mock
    private StarRepository starRepository; // ✅ REQUIRED (prevents NPE)

    @InjectMocks
    private PublicDiagramServiceImpl service;

    private UUID diagramId;
    private Diagram diagram;
    private PublicDiagram publicDiagram;

    @BeforeEach
    void setup() {
        diagramId = UUID.randomUUID();

        diagram = Diagram.builder()
                .id(diagramId)
                .name("Test Diagram")
                .ddl("CREATE TABLE x (id INT);")
                .build();

        publicDiagram = new PublicDiagram();
        publicDiagram.setId(diagramId);
        publicDiagram.setDiagram(diagram);
        publicDiagram.setStars(0);
        publicDiagram.setForks(0);
        publicDiagram.setViews(0);
        publicDiagram.setShortDescription("short");
        publicDiagram.setDetailedDescription("detailed");

        publicDiagram.setHashtags(new HashSet<>());
        publicDiagram.setCannedQueries(new HashSet<>());
    }

    // ------------------------------------------------------------
    // 1) Diagram not found → exception
    // ------------------------------------------------------------
    @Test
    void view_missingDiagram_throws() {
        when(userDiagramService.getDiagramOrThrow(diagramId))
                .thenThrow(new PublicDiagramException.DiagramNotFoundException("not found"));

        assertThrows(PublicDiagramException.DiagramNotFoundException.class,
                () -> service.viewPublicDiagram(1, diagramId));

        verify(userDiagramService).getDiagramOrThrow(diagramId);
        verifyNoMoreInteractions(userDiagramService);
    }

    // ------------------------------------------------------------
    // 2) Diagram exists but no public diagram → exception
    // ------------------------------------------------------------
    @Test
    void view_missingPublicDiagram_throws() {
        when(userDiagramService.getDiagramOrThrow(diagramId)).thenReturn(diagram);

        assertThrows(PublicDiagramException.DiagramNotFoundException.class,
                () -> service.viewPublicDiagram(2, diagramId));

        verify(userDiagramService).getDiagramOrThrow(diagramId);
        verifyNoInteractions(viewsService);
    }

    // ------------------------------------------------------------
    // 3) First time view → increment views
    // ------------------------------------------------------------
    @Test
    void view_firstTime_increments() {
        diagram.setPublicDiagram(publicDiagram);

        when(userDiagramService.getDiagramOrThrow(diagramId)).thenReturn(diagram);
        when(viewsService.addView(7, diagramId)).thenReturn(true);
        when(userDiagramService.getContributors(diagramId))
                .thenReturn(List.of(new ContributorDto("Alice", "pic", null)));

        // star check (not used in this test but required by service)
        when(starRepository.existsById(any())).thenReturn(false);

        PublicDiagramDto dto = service.viewPublicDiagram(7, diagramId);

        assertEquals(1, publicDiagram.getViews());
        assertEquals(1, dto.getViews());

        verify(userDiagramService).getDiagramOrThrow(diagramId);
        verify(viewsService).addView(7, diagramId);
        verify(userDiagramService).getContributors(diagramId);
    }

    // ------------------------------------------------------------
    // 4) Second view by same user → no increment
    // ------------------------------------------------------------
    @Test
    void view_secondTime_noIncrement() {
        diagram.setPublicDiagram(publicDiagram);
        publicDiagram.setViews(5);

        when(userDiagramService.getDiagramOrThrow(diagramId)).thenReturn(diagram);
        when(viewsService.addView(11, diagramId)).thenReturn(false);
        when(userDiagramService.getContributors(diagramId))
                .thenReturn(Collections.emptyList());
        when(starRepository.existsById(any())).thenReturn(false);

        PublicDiagramDto dto = service.viewPublicDiagram(11, diagramId);

        assertEquals(5, publicDiagram.getViews());
        assertEquals(5, dto.getViews());
    }

    // ------------------------------------------------------------
    // 5) View twice → only increment once
    // ------------------------------------------------------------
    @Test
    void view_twice_incrementOnlyOnce() {
        diagram.setPublicDiagram(publicDiagram);

        when(userDiagramService.getDiagramOrThrow(diagramId)).thenReturn(diagram);
        when(viewsService.addView(21, diagramId))
                .thenReturn(true) // first time
                .thenReturn(false); // second time
        when(userDiagramService.getContributors(diagramId))
                .thenReturn(Collections.emptyList());
        when(starRepository.existsById(any())).thenReturn(false);

        PublicDiagramDto dto1 = service.viewPublicDiagram(21, diagramId);
        PublicDiagramDto dto2 = service.viewPublicDiagram(21, diagramId);

        assertEquals(1, publicDiagram.getViews());
        assertEquals(1, dto1.getViews());
        assertEquals(1, dto2.getViews());
    }

    // ------------------------------------------------------------
    // 6) Contributors included
    // ------------------------------------------------------------
    @Test
    void view_includesContributors() {
        diagram.setPublicDiagram(publicDiagram);

        List<ContributorDto> contributors = List.of(
                new ContributorDto("Bob", "p1", null),
                new ContributorDto("Carol", "p2", null));

        when(userDiagramService.getDiagramOrThrow(diagramId)).thenReturn(diagram);
        when(viewsService.addView(99, diagramId)).thenReturn(false);
        when(userDiagramService.getContributors(diagramId)).thenReturn(contributors);
        when(starRepository.existsById(any())).thenReturn(false);

        PublicDiagramDto dto = service.viewPublicDiagram(99, diagramId);

        assertEquals(2, dto.getContributors().size());
        assertEquals("Bob", dto.getContributors().get(0).getName());
    }

    // ------------------------------------------------------------
    // 7) viewsService throws → propagate
    // ------------------------------------------------------------
    @Test
    void view_viewsServiceThrows_propagates() {
        diagram.setPublicDiagram(publicDiagram);

        when(userDiagramService.getDiagramOrThrow(diagramId)).thenReturn(diagram);
        when(viewsService.addView(42, diagramId))
                .thenThrow(new RuntimeException("views db down"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.viewPublicDiagram(42, diagramId));

        assertEquals("views db down", ex.getMessage());
    }
}
