package backend.publicDiagramManagement.PublucDiagramServiceTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backend.entities.Diagram;
import backend.entities.User;
import backend.entities.joins.UserDiagram;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.exceptions.PublicDiagramException;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.publicDiagramManagement.repository.StarRepository;
import backend.publicDiagramManagement.repository.ForkRepository;
import backend.publicDiagramManagement.repository.ViewsRepository;
import backend.publicDiagramManagement.service.PublicDiagramServiceImpl;
import backend.user.Role;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.repository.UserDiagramRepository;
import backend.userDiagramManagement.service.UserDiagramService;

class UnpublishTests {

    private UserDiagramRepository userDiagramRepository;
    private DiagramRepository diagramRepository;
    private PublicDiagramRepository publicDiagramRepository;
    private UserDiagramService userDiagramService;

    private ViewsRepository viewsRepository;
    private StarRepository starRepository;
    private ForkRepository forkRepository;

    private PublicDiagramServiceImpl service;

    private final int USER_ID = 1;
    private final UUID DIAGRAM_ID = UUID.randomUUID();

    private User owner;
    private Diagram diagram;
    private PublicDiagram publicDiagram;
    private UserDiagram ownerLink;

    @BeforeEach
    void setup() {
        // Mock all dependencies
        userDiagramRepository = mock(UserDiagramRepository.class);
        diagramRepository = mock(DiagramRepository.class);
        publicDiagramRepository = mock(PublicDiagramRepository.class);
        userDiagramService = mock(UserDiagramService.class);

        viewsRepository = mock(ViewsRepository.class);
        starRepository = mock(StarRepository.class);
        forkRepository = mock(ForkRepository.class);

        service = new PublicDiagramServiceImpl(
                userDiagramRepository,
                diagramRepository,
                publicDiagramRepository,
                userDiagramService,
                null,  // HashtagService
                null,  // ViewsService
                viewsRepository,
                forkRepository,
                starRepository,
                null,  // UserService
                null   // UserRepository
        );

        // Setup test entities
        owner = new User();
        owner.setId(USER_ID);

        diagram = new Diagram();
        diagram.setId(DIAGRAM_ID);

        publicDiagram = new PublicDiagram();
        publicDiagram.setId(DIAGRAM_ID);
        publicDiagram.setDiagram(diagram);

        diagram.setPublicDiagram(publicDiagram);

        ownerLink = UserDiagram.builder()
                .user(owner)
                .diagram(diagram)
                .role(Role.OWNER)
                .build();
    }

    // -------------------------------------------------------------
    // ✅ Test: Owner successfully unpublishes diagram
    // -------------------------------------------------------------
    @Test
    void unpublish_success() {
        when(userDiagramService.getUserDiagramOrThrow(USER_ID, DIAGRAM_ID))
                .thenReturn(ownerLink);

        doNothing().when(viewsRepository).deleteByIdPublicDiagramId(DIAGRAM_ID);
        doNothing().when(starRepository).deleteByIdPublicDiagramId(DIAGRAM_ID);
        doNothing().when(forkRepository).deleteByOriginalDiagramId(DIAGRAM_ID);
        doNothing().when(publicDiagramRepository).deleteById(DIAGRAM_ID);

        service.unPublishPublicDiagram(USER_ID, DIAGRAM_ID);

        // Verify all deletes called
        verify(viewsRepository, times(1)).deleteByIdPublicDiagramId(DIAGRAM_ID);
        verify(starRepository, times(1)).deleteByIdPublicDiagramId(DIAGRAM_ID);
        verify(forkRepository, times(1)).deleteByOriginalDiagramId(DIAGRAM_ID);
        verify(publicDiagramRepository, times(1)).deleteById(DIAGRAM_ID);

        // Diagram should no longer reference the public diagram
        assertNull(diagram.getPublicDiagram());
    }

    // -------------------------------------------------------------
    // ✅ Test: Diagram is not public → throws
    // -------------------------------------------------------------
    @Test
    void unpublish_notPublicDiagram_throws() {
        diagram.setPublicDiagram(null);

        when(userDiagramService.getUserDiagramOrThrow(USER_ID, DIAGRAM_ID))
                .thenReturn(ownerLink);

        PublicDiagramException.DiagramNotFoundException ex = assertThrows(
                PublicDiagramException.DiagramNotFoundException.class,
                () -> service.unPublishPublicDiagram(USER_ID, DIAGRAM_ID));

        assertTrue(ex.getMessage().contains("not public"));
    }

    // -------------------------------------------------------------
    // ✅ Test: Non-owner cannot unpublish
    // -------------------------------------------------------------
    @Test
    void unpublish_notOwner_throwsPermissionDenied() {
        UserDiagram readerLink = UserDiagram.builder()
                .user(owner)
                .diagram(diagram)
                .role(Role.READER)
                .build();

        when(userDiagramService.getUserDiagramOrThrow(USER_ID, DIAGRAM_ID))
                .thenReturn(readerLink);

        doThrow(new RuntimeException("Only owner can unpublish"))
                .when(userDiagramService)
                .checkOwner(readerLink, "unpublish");

        assertThrows(RuntimeException.class,
                () -> service.unPublishPublicDiagram(USER_ID, DIAGRAM_ID));
    }

    // -------------------------------------------------------------
    // ✅ Test: Repository delete is actually called with correct entity
    // -------------------------------------------------------------
    @Test
    void unpublish_repositoryDeleteByIdCalled() {
        when(userDiagramService.getUserDiagramOrThrow(USER_ID, DIAGRAM_ID))
                .thenReturn(ownerLink);

        doNothing().when(viewsRepository).deleteByIdPublicDiagramId(DIAGRAM_ID);
        doNothing().when(starRepository).deleteByIdPublicDiagramId(DIAGRAM_ID);
        doNothing().when(forkRepository).deleteByOriginalDiagramId(DIAGRAM_ID);
        doNothing().when(publicDiagramRepository).deleteById(DIAGRAM_ID);

        service.unPublishPublicDiagram(USER_ID, DIAGRAM_ID);

        verify(publicDiagramRepository, times(1))
                .deleteById(publicDiagram.getId());
    }

    // -------------------------------------------------------------
    // ✅ Test: After deletion, diagram.publicDiagram is null
    // -------------------------------------------------------------
    @Test
    void unpublish_clearsPublicDiagramField() {
        when(userDiagramService.getUserDiagramOrThrow(USER_ID, DIAGRAM_ID))
                .thenReturn(ownerLink);

        doNothing().when(viewsRepository).deleteByIdPublicDiagramId(DIAGRAM_ID);
        doNothing().when(starRepository).deleteByIdPublicDiagramId(DIAGRAM_ID);
        doNothing().when(forkRepository).deleteByOriginalDiagramId(DIAGRAM_ID);
        doNothing().when(publicDiagramRepository).deleteById(DIAGRAM_ID);

        service.unPublishPublicDiagram(USER_ID, DIAGRAM_ID);

        assertNull(diagram.getPublicDiagram());
    }
}
