package backend.publicDiagramManagement.PublicDiagramServiceTest;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.entities.Diagram;
import backend.entities.joins.UserDiagram;
import backend.entities.publicDiagramEntities.CannedQueriesDiagrams;
import backend.entities.publicDiagramEntities.Hashtag;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.dto.DiagramCannedQueryDto;
import backend.publicDiagramManagement.dto.publish.PublishDiagramRequestDto;
import backend.publicDiagramManagement.exceptions.PublicDiagramException;
import backend.publicDiagramManagement.repository.ForkRepository;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.publicDiagramManagement.repository.ViewsRepository;
import backend.publicDiagramManagement.service.HashtagService;
import backend.publicDiagramManagement.service.PublicDiagramServiceImpl;
import backend.publicDiagramManagement.service.ViewsService;
import backend.user.Role;
import backend.user.UserService;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.repository.UserDiagramRepository;
import backend.userDiagramManagement.service.UserDiagramService;

@ExtendWith(MockitoExtension.class)
class PublishTests {

    @Mock
    private UserDiagramRepository userDiagramRepository;
    @Mock
    private DiagramRepository diagramRepository;
    @Mock
    private PublicDiagramRepository publicDiagramRepository;
    @Mock
    private UserDiagramService userDiagramService;
    @Mock
    private HashtagService hashtagService;
    @Mock
    private ViewsService viewsService;
    @Mock
    private ViewsRepository viewsRepository;
    @Mock
    private ForkRepository forkRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private PublicDiagramServiceImpl service;

    private PublishDiagramRequestDto dto;
    private UUID diagramId;
    private Diagram diagram;
    private UserDiagram userDiagram;

    @BeforeEach
    void setup() {
        diagramId = UUID.randomUUID();

        dto = new PublishDiagramRequestDto();
        dto.setDiagramId(diagramId);
        dto.setShortDescription("short");
        dto.setDetailedDescription("detailed");
        dto.setHashTags(List.of("#sql", "#erd"));
        dto.setCannedQueries(List.of(new DiagramCannedQueryDto("SELECT 1", "Q1", "desc", 1)));

        diagram = Diagram.builder().id(diagramId).build();

        userDiagram = UserDiagram.builder()
                .diagram(diagram)
                .role(Role.OWNER)
                .build();
    }

    // ----------------------------------------------------------
    // 1. Diagram NOT found → Exception
    // ----------------------------------------------------------
    @Test
    void publishDiagram_diagramNotFound_throws() {
        when(userDiagramService.getUserDiagramOrThrow(anyInt(), eq(diagramId)))
                .thenThrow(new PublicDiagramException.DiagramNotFoundException("not found"));

        assertThrows(PublicDiagramException.DiagramNotFoundException.class,
                () -> service.publishDiagram(5, dto));
    }

    // ----------------------------------------------------------
    // 2. User NOT owner → PermissionDenied
    // ----------------------------------------------------------
    @Test
    void publishDiagram_notOwner_throws() {
        userDiagram.setRole(Role.READER);

        when(userDiagramService.getUserDiagramOrThrow(anyInt(), eq(diagramId)))
                .thenReturn(userDiagram);

        doThrow(new PublicDiagramException.PermissionDeniedException("not owner"))
                .when(userDiagramService).checkOwner(eq(userDiagram), anyString());

        assertThrows(PublicDiagramException.PermissionDeniedException.class,
                () -> service.publishDiagram(5, dto));
    }

    // ----------------------------------------------------------
    // 3. Publish NEW public diagram
    // ----------------------------------------------------------
    @Test
    void publishDiagram_createsNewPublicDiagram() {
        when(userDiagramService.getUserDiagramOrThrow(anyInt(), eq(diagramId)))
                .thenReturn(userDiagram);

        when(publicDiagramRepository.findById(diagramId))
                .thenReturn(Optional.empty());

        when(hashtagService.resolveHashtags(any()))
                .thenReturn(Set.of(new Hashtag(1, "sql", new HashSet<>())));

        service.publishDiagram(5, dto);

        verify(publicDiagramRepository, times(1)).save(any(PublicDiagram.class));
    }

    // ----------------------------------------------------------
    // 4. Update EXISTING public diagram
    // ----------------------------------------------------------
    @Test
    void publishDiagram_updatesExistingDiagram() {
        when(userDiagramService.getUserDiagramOrThrow(anyInt(), eq(diagramId)))
                .thenReturn(userDiagram);

        PublicDiagram existing = new PublicDiagram();
        existing.setId(diagramId);
        existing.setDiagram(diagram);
        existing.setShortDescription("old");

        when(publicDiagramRepository.findById(diagramId))
                .thenReturn(Optional.of(existing));

        when(hashtagService.resolveHashtags(any())).thenReturn(Set.of());

        service.publishDiagram(10, dto);

        assertEquals("short", existing.getShortDescription());
        assertEquals("detailed", existing.getDetailedDescription());
    }

    // ----------------------------------------------------------
    // 5. Replace canned queries
    // ----------------------------------------------------------
    @Test
    void publishDiagram_replacesCannedQueries() {
        when(userDiagramService.getUserDiagramOrThrow(anyInt(), eq(diagramId)))
                .thenReturn(userDiagram);

        PublicDiagram existing = new PublicDiagram();
        existing.setId(diagramId);
        existing.setDiagram(diagram);
        existing.setCannedQueries(new HashSet<>(Set.of(new CannedQueriesDiagrams())));

        when(publicDiagramRepository.findById(diagramId))
                .thenReturn(Optional.of(existing));

        when(hashtagService.resolveHashtags(any())).thenReturn(Set.of());

        service.publishDiagram(1, dto);

        assertEquals(1, existing.getCannedQueries().size());
    }

    // ----------------------------------------------------------
    // 6. Hashtags resolved and stored
    // ----------------------------------------------------------
    @Test
    void publishDiagram_resolvesHashtags() {
        when(userDiagramService.getUserDiagramOrThrow(anyInt(), eq(diagramId)))
                .thenReturn(userDiagram);

        when(publicDiagramRepository.findById(diagramId))
                .thenReturn(Optional.empty());

        Set<Hashtag> resolved = Set.of(new Hashtag(10, "sql", new HashSet<>()));
        when(hashtagService.resolveHashtags(any())).thenReturn(resolved);

        service.publishDiagram(5, dto);

        ArgumentCaptor<PublicDiagram> captor = ArgumentCaptor.forClass(PublicDiagram.class);

        verify(publicDiagramRepository).save(captor.capture());

        assertEquals(resolved, captor.getValue().getHashtags());
    }

    // ----------------------------------------------------------
    // 7. Empty hashtags
    // ----------------------------------------------------------
    @Test
    void publishDiagram_emptyHashtags() {
        dto.setHashTags(Collections.emptyList());

        when(userDiagramService.getUserDiagramOrThrow(anyInt(), eq(diagramId)))
                .thenReturn(userDiagram);

        when(publicDiagramRepository.findById(diagramId))
                .thenReturn(Optional.empty());

        when(hashtagService.resolveHashtags(any())).thenReturn(Set.of());

        service.publishDiagram(1, dto);

        verify(publicDiagramRepository).save(any());
    }

    // ----------------------------------------------------------
    // 8. Null canned queries → NPE (expected by current implementation)
    // ----------------------------------------------------------
    @Test
    void publishDiagram_nullCannedQueries_throwsNPE() {
        dto.setCannedQueries(null);

        when(userDiagramService.getUserDiagramOrThrow(anyInt(), eq(diagramId)))
                .thenReturn(userDiagram);

        assertThrows(NullPointerException.class,
                () -> service.publishDiagram(1, dto));
    }

    // ----------------------------------------------------------
    // 9. Duplicate hashtags
    // ----------------------------------------------------------
    @Test
    void publishDiagram_duplicateHashtags() {
        dto.setHashTags(List.of("#sql", "#sql"));

        when(userDiagramService.getUserDiagramOrThrow(anyInt(), eq(diagramId)))
                .thenReturn(userDiagram);

        when(publicDiagramRepository.findById(diagramId))
                .thenReturn(Optional.empty());

        when(hashtagService.resolveHashtags(any()))
                .thenReturn(Set.of(new Hashtag(1, "sql", new HashSet<>())));

        service.publishDiagram(1, dto);

        verify(publicDiagramRepository).save(any());
    }

    // ----------------------------------------------------------
    // 10. Resolver returns empty set
    // ----------------------------------------------------------
    @Test
    void publishDiagram_hashtagResolverReturnsEmpty() {
        when(userDiagramService.getUserDiagramOrThrow(anyInt(), eq(diagramId)))
                .thenReturn(userDiagram);

        when(publicDiagramRepository.findById(diagramId))
                .thenReturn(Optional.empty());

        when(hashtagService.resolveHashtags(any()))
                .thenReturn(Collections.emptySet());

        service.publishDiagram(2, dto);

        verify(publicDiagramRepository).save(any());
    }

    // ----------------------------------------------------------
    // 11. Save throws → propagate
    // ----------------------------------------------------------
    @Test
    void publishDiagram_repositorySaveFails() {
        when(userDiagramService.getUserDiagramOrThrow(anyInt(), eq(diagramId)))
                .thenReturn(userDiagram);

        when(publicDiagramRepository.findById(diagramId))
                .thenReturn(Optional.empty());

        when(hashtagService.resolveHashtags(any())).thenReturn(Set.of());

        when(publicDiagramRepository.save(any()))
                .thenThrow(new RuntimeException("db down"));

        assertThrows(RuntimeException.class,
                () -> service.publishDiagram(1, dto));
    }

    // ----------------------------------------------------------
    // 12. Existing diagram with null fields → safe update
    // ----------------------------------------------------------
    @Test
    void publishDiagram_existingWithNullFields() {
        when(userDiagramService.getUserDiagramOrThrow(anyInt(), eq(diagramId)))
                .thenReturn(userDiagram);

        PublicDiagram existing = new PublicDiagram();
        existing.setId(diagramId);
        existing.setDiagram(diagram);
        existing.setHashtags(null);
        existing.setCannedQueries(null);

        when(publicDiagramRepository.findById(diagramId))
                .thenReturn(Optional.of(existing));

        when(hashtagService.resolveHashtags(any())).thenReturn(Set.of());

        service.publishDiagram(3, dto);

        assertNotNull(existing.getHashtags());
        assertNotNull(existing.getCannedQueries());
    }

    // ----------------------------------------------------------
    // 13. Publish twice → two saves
    // ----------------------------------------------------------
    @Test
    void publishDiagram_publishTwice() {
        when(userDiagramService.getUserDiagramOrThrow(anyInt(), eq(diagramId)))
                .thenReturn(userDiagram);

        PublicDiagram existing = new PublicDiagram();
        existing.setId(diagramId);
        existing.setDiagram(diagram);

        when(publicDiagramRepository.findById(diagramId))
                .thenReturn(Optional.empty()) // first time
                .thenReturn(Optional.of(existing)); // second time

        when(hashtagService.resolveHashtags(any())).thenReturn(Set.of());

        service.publishDiagram(1, dto);
        service.publishDiagram(1, dto);

        verify(publicDiagramRepository, times(2)).save(any());
    }
}
