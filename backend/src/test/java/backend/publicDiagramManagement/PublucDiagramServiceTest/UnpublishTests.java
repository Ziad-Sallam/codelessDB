package backend.publicDiagramManagement.PublucDiagramServiceTest;

import backend.entities.Diagram;
import backend.entities.User;
import backend.entities.joins.UserDiagram;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.service.PublicDiagramServiceImpl;
import backend.user.Role;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.repository.UserDiagramRepository;
import backend.userDiagramManagement.service.UserDiagramService;
import backend.publicDiagramManagement.exceptions.PublicDiagramException;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnpublishTests {

    private UserDiagramRepository userDiagramRepository;
    private DiagramRepository diagramRepository;
    private PublicDiagramRepository publicDiagramRepository;
    private UserDiagramService userDiagramService;
    private PublicDiagramServiceImpl service;

    private final int USER_ID = 1;
    private final UUID DIAGRAM_ID = UUID.randomUUID();

    private User owner;
    private Diagram diagram;
    private PublicDiagram publicDiagram;
    private UserDiagram ownerLink;

    @BeforeEach
    void setup() {
        userDiagramRepository = mock(UserDiagramRepository.class);
        diagramRepository = mock(DiagramRepository.class);
        publicDiagramRepository = mock(PublicDiagramRepository.class);
        userDiagramService = mock(UserDiagramService.class);

        service = new PublicDiagramServiceImpl(
                userDiagramRepository,
                diagramRepository,
                publicDiagramRepository,
                userDiagramService,
                null, null, null,
                null, null, null, null
        );

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

        // delete() should be called
        doNothing().when(publicDiagramRepository).delete(publicDiagram);

        service.unPublishPublicDiagram(USER_ID, DIAGRAM_ID);

        // diagram no longer has public entry
        assertNull(diagram.getPublicDiagram());

        verify(publicDiagramRepository, times(1)).delete(publicDiagram);
    }

    // -------------------------------------------------------------
    // ✅ Test: Diagram is not public → throws
    // -------------------------------------------------------------
    @Test
    void unpublish_notPublicDiagram_throws() {
        diagram.setPublicDiagram(null);

        when(userDiagramService.getUserDiagramOrThrow(USER_ID, DIAGRAM_ID))
                .thenReturn(ownerLink);

        PublicDiagramException.DiagramNotFoundException ex =
                assertThrows(
                        PublicDiagramException.DiagramNotFoundException.class,
                        () -> service.unPublishPublicDiagram(USER_ID, DIAGRAM_ID)
                );

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
    void unpublish_repositoryDeleteCalled() {
        when(userDiagramService.getUserDiagramOrThrow(USER_ID, DIAGRAM_ID))
                .thenReturn(ownerLink);

        service.unPublishPublicDiagram(USER_ID, DIAGRAM_ID);

        ArgumentCaptor<PublicDiagram> captor =
                ArgumentCaptor.forClass(PublicDiagram.class);

        verify(publicDiagramRepository, times(1)).delete(captor.capture());

        assertEquals(publicDiagram, captor.getValue());
    }

    // -------------------------------------------------------------
    // ✅ Test: After deletion, diagram.publicDiagram is null
    // -------------------------------------------------------------
    @Test
    void unpublish_clearsPublicDiagramField() {
        when(userDiagramService.getUserDiagramOrThrow(USER_ID, DIAGRAM_ID))
                .thenReturn(ownerLink);

        service.unPublishPublicDiagram(USER_ID, DIAGRAM_ID);

        assertNull(diagram.getPublicDiagram());
    }
}

