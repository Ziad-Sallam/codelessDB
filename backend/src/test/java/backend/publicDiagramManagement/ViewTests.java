package backend.publicDiagramManagement;

import backend.entities.Diagram;
import backend.entities.publicDiagramEntities.Hashtag;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.dto.PublicDiagramDto;
import backend.publicDiagramManagement.exceptions.PublicDiagramException;
import backend.publicDiagramManagement.service.PublicDiagramServiceImpl;
import backend.publicDiagramManagement.service.ViewsService;
import backend.user.UserService;
import backend.userDiagramManagement.dto.ContributorDto;
import backend.userDiagramManagement.service.UserDiagramService;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.repository.UserDiagramRepository;
import backend.publicDiagramManagement.service.HashtagService;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.publicDiagramManagement.repository.ViewsRepository;
import backend.publicDiagramManagement.repository.ForkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewTests {

    // Mocks to satisfy the service constructor (only the ones used in tests will be stubbed)
    @Mock private UserDiagramRepository userDiagramRepository;
    @Mock private DiagramRepository diagramRepository;
    @Mock private PublicDiagramRepository publicDiagramRepository;
    @Mock private UserDiagramService userDiagramService;
    @Mock private HashtagService hashtagService;
    @Mock private ViewsService viewsService;
    @Mock private ViewsRepository viewsRepository;
    @Mock private ForkRepository forkRepository;
    @Mock private UserService userService;

    @InjectMocks
    private PublicDiagramServiceImpl service;

    private UUID diagramId;
    private Diagram diagram;
    private PublicDiagram publicDiagram;

    @BeforeEach
    void setup() {
        diagramId = UUID.randomUUID();

        // create diagram (no publicDiagram by default)
        diagram = Diagram.builder()
                .id(diagramId)
                .name("Test")
                .ddl("CREATE TABLE x (id INT);")
                .build();

        // create a PublicDiagram but do not attach it to diagram unless the test requires it
        publicDiagram = new PublicDiagram();
        publicDiagram.setId(diagramId);
        publicDiagram.setDiagram(diagram);
        publicDiagram.setShortDescription("short");
        publicDiagram.setDetailedDescription("detailed");
        publicDiagram.setStars(0);
        publicDiagram.setForks(0);
        publicDiagram.setViews(0);

        // ensure sets are non-null to avoid NPE during DTO conversion
        publicDiagram.setHashtags(new HashSet<>());
        publicDiagram.setCannedQueries(new HashSet<>());
    }

    // ----------------------------
    // 1) diagram missing -> throws
    // ----------------------------
    @Test
    void view_missingDiagram_throws() {
        // userDiagramService.getDiagramOrThrow will throw -> service should propagate it
        when(userDiagramService.getDiagramOrThrow(diagramId))
                .thenThrow(new PublicDiagramException.DiagramNotFoundException("diagram not found"));

        assertThrows(PublicDiagramException.DiagramNotFoundException.class,
                () -> service.viewPublicDiagram(1, diagramId));

        // verify no other interactions
        verify(userDiagramService).getDiagramOrThrow(diagramId);
        verifyNoMoreInteractions(userDiagramService, viewsService);
    }

    // ------------------------------------------------
    // 2) diagram exists but no public diagram -> throw
    // ------------------------------------------------
    @Test
    void view_missingPublicDiagram_throws() {
        // diagram is returned but has no publicDiagram attached
        when(userDiagramService.getDiagramOrThrow(diagramId)).thenReturn(diagram);

        // diagram.publicDiagram is null by default -> service should throw DiagramNotFoundException
        assertThrows(PublicDiagramException.DiagramNotFoundException.class,
                () -> service.viewPublicDiagram(2, diagramId));

        verify(userDiagramService).getDiagramOrThrow(diagramId);
        verifyNoInteractions(viewsService);
    }

    // ------------------------------------------------------------
    // 3) first time view -> views incremented (viewsService returns true)
    // ------------------------------------------------------------
    @Test
    void view_firstTime_increments() {
        // attach public diagram to diagram (so getPublicDiagramOrThrow succeeds)
        diagram.setPublicDiagram(publicDiagram);

        when(userDiagramService.getDiagramOrThrow(diagramId)).thenReturn(diagram);
        when(viewsService.addView(7, diagramId)).thenReturn(true);
        // contributors (can be empty)
        List<ContributorDto> contributors = List.of(new ContributorDto("Alice", "pic", null));
        when(userDiagramService.getContributors(diagramId)).thenReturn(contributors);

        // call
        PublicDiagramDto dto = service.viewPublicDiagram(7, diagramId);

        // service should have incremented the in-memory publicDiagram.views by 1
        assertEquals(1, publicDiagram.getViews());
        assertEquals(1, dto.getViews());

        verify(userDiagramService).getDiagramOrThrow(diagramId);
        verify(viewsService).addView(7, diagramId);
        verify(userDiagramService).getContributors(diagramId);
    }

    // ------------------------------------------------------------
    // 4) second view by same user -> viewsService returns false -> no increment
    // ------------------------------------------------------------
    @Test
    void view_secondTime_noIncrement() {
        diagram.setPublicDiagram(publicDiagram);
        publicDiagram.setViews(5); // starting value

        when(userDiagramService.getDiagramOrThrow(diagramId)).thenReturn(diagram);
        when(viewsService.addView(11, diagramId)).thenReturn(false);
        when(userDiagramService.getContributors(diagramId)).thenReturn(Collections.emptyList());

        PublicDiagramDto dto = service.viewPublicDiagram(11, diagramId);

        assertEquals(5, publicDiagram.getViews()); // unchanged
        assertEquals(5, dto.getViews());

        verify(userDiagramService).getDiagramOrThrow(diagramId);
        verify(viewsService).addView(11, diagramId);
        verify(userDiagramService).getContributors(diagramId);
    }

    // ------------------------------------------------------------
    // 5) view twice (same user) -> viewsService first returns true then false -> increment only once
    // ------------------------------------------------------------
    @Test
    void view_twice_incrementOnlyOnce() {
        diagram.setPublicDiagram(publicDiagram);
        publicDiagram.setViews(0);

        when(userDiagramService.getDiagramOrThrow(diagramId)).thenReturn(diagram);
        // simulate first call -> new view true, second call -> already viewed false
        when(viewsService.addView(21, diagramId)).thenReturn(true).thenReturn(false);
        when(userDiagramService.getContributors(diagramId)).thenReturn(Collections.emptyList());

        // first call
        PublicDiagramDto dto1 = service.viewPublicDiagram(21, diagramId);
        // second call
        PublicDiagramDto dto2 = service.viewPublicDiagram(21, diagramId);

        // only increment once
        assertEquals(1, publicDiagram.getViews());
        assertEquals(1, dto1.getViews());
        assertEquals(1, dto2.getViews());

        verify(userDiagramService, times(2)).getDiagramOrThrow(diagramId);
        verify(viewsService, times(2)).addView(21, diagramId);
        verify(userDiagramService, times(2)).getContributors(diagramId);
    }

    // ------------------------------------------------------------
    // 6) returned DTO includes contributors unchanged
    // ------------------------------------------------------------
    @Test
    void view_includesContributors() {
        diagram.setPublicDiagram(publicDiagram);

        List<ContributorDto> contributors = List.of(
                new ContributorDto("Bob", "p1", null),
                new ContributorDto("Carol", "p2", null)
        );

        when(userDiagramService.getDiagramOrThrow(diagramId)).thenReturn(diagram);
        when(viewsService.addView(99, diagramId)).thenReturn(false);
        when(userDiagramService.getContributors(diagramId)).thenReturn(contributors);

        PublicDiagramDto dto = service.viewPublicDiagram(99, diagramId);

        assertNotNull(dto.getContributors());
        assertEquals(2, dto.getContributors().size());
        assertEquals("Bob", dto.getContributors().get(0).getName());

        verify(userDiagramService).getDiagramOrThrow(diagramId);
        verify(viewsService).addView(99, diagramId);
        verify(userDiagramService).getContributors(diagramId);
    }

    // ------------------------------------------------------------
    // 7) viewsService throws -> exception propagates
    // ------------------------------------------------------------
    @Test
    void view_viewsServiceThrows_propagates() {
        diagram.setPublicDiagram(publicDiagram);

        when(userDiagramService.getDiagramOrThrow(diagramId)).thenReturn(diagram);
        when(viewsService.addView(42, diagramId)).thenThrow(new RuntimeException("views db down"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.viewPublicDiagram(42, diagramId));

        assertEquals("views db down", ex.getMessage());

        verify(userDiagramService).getDiagramOrThrow(diagramId);
        verify(viewsService).addView(42, diagramId);
        verifyNoMoreInteractions(userDiagramService);
    }
}
