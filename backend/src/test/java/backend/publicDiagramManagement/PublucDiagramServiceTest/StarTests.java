package backend.publicDiagramManagement.PublucDiagramServiceTest;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import backend.entities.Diagram;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.dto.PublicDiagramInfoDto;
import backend.publicDiagramManagement.exceptions.PublicDiagramException;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.publicDiagramManagement.repository.StarRepository;
import backend.publicDiagramManagement.service.PublicDiagramServiceImpl;
import backend.user.Role;
import backend.userDiagramManagement.dto.ContributorDto;
import backend.userDiagramManagement.service.UserDiagramService;

@ExtendWith(MockitoExtension.class)
public class StarTests {

    @Mock
    private UserDiagramService userDiagramService;
    @Mock
    private PublicDiagramRepository publicDiagramRepository;
    @Mock
    private StarRepository starRepository;

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
                .name("Original")
                .build();

        publicDiagram = PublicDiagram.builder()
                .id(diagramId)
                .diagram(diagram)
                .stars(0)
                .build();

        diagram.setPublicDiagram(publicDiagram);
    }

    /*
     * ----------------------------------------------------------
     * 1. starPublicDiagram → inserts record + increments star
     * ----------------------------------------------------------
     */
    @Test
    void star_success_createsRecord_and_increments() {

        when(userDiagramService.getDiagramOrThrow(diagramId))
                .thenReturn(diagram);

        // star does not exist yet
        when(starRepository.existsById(any())).thenReturn(false);

        service.starPublicDiagram(5, diagramId);

        // record created
        verify(starRepository).save(any());

        // star count incremented
        verify(publicDiagramRepository).incrementStar(diagramId);
    }

    /*
     * ----------------------------------------------------------
     * 2. starPublicDiagram → duplicate star → no-op
     * ----------------------------------------------------------
     */
    @Test
    void star_duplicate_noOp() {

        when(userDiagramService.getDiagramOrThrow(diagramId))
                .thenReturn(diagram);

        // already starred → do nothing
        when(starRepository.existsById(any())).thenReturn(true);

        service.starPublicDiagram(9, diagramId);

        verify(starRepository, never()).save(any());
        verify(publicDiagramRepository, never()).incrementStar(any());
    }

    /*
     * ----------------------------------------------------------
     * 3. unstarPublicDiagram → deletes and decrements
     * ----------------------------------------------------------
     */
    @Test
    void unstar_success_deletes_and_decrements() {

        when(userDiagramService.getDiagramOrThrow(diagramId))
                .thenReturn(diagram);

        when(starRepository.existsById(any())).thenReturn(true);

        service.unstarPublicDiagram(4, diagramId);

        verify(starRepository).deleteById(any());
        verify(publicDiagramRepository).decrementStar(diagramId);
    }

    /*
     * ----------------------------------------------------------
     * 4. unstar when not starred → no-op
     * ----------------------------------------------------------
     */
    @Test
    void unstar_notStarred_noOp() {

        when(userDiagramService.getDiagramOrThrow(diagramId))
                .thenReturn(diagram);

        when(starRepository.existsById(any())).thenReturn(false);

        service.unstarPublicDiagram(4, diagramId);

        verify(starRepository, never()).deleteById(any());
        verify(publicDiagramRepository, never()).decrementStar(any());
    }

    /*
     * ----------------------------------------------------------
     * 5. getStaredPublicDiagrams → returns DTO with contributors
     * ----------------------------------------------------------
     */
    @Test
    void getStarredPublicDiagrams_returnsDto() {

        PublicDiagram pd = PublicDiagram.builder()
                .id(diagramId)
                .diagram(diagram)
                .stars(10)
                .views(20)
                .shortDescription("short")
                .build();

        Page<PublicDiagram> page = new PageImpl<>(
                List.of(pd),
                PageRequest.of(0, 10),
                1);

        when(starRepository.findStarredPublicDiagramsByUser(7, PageRequest.of(0, 10)))
                .thenReturn(page);

        List<ContributorDto> contributors = List.of(
                new ContributorDto("Alice", "pic", Role.OWNER));

        when(userDiagramService.getContributors(diagramId))
                .thenReturn(contributors);

        Page<PublicDiagramInfoDto> result = service.getStaredPublicDiagrams(7, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());

        PublicDiagramInfoDto dto = result.getContent().get(0);

        assertEquals("short", dto.getShortDescription());
        assertEquals(10, dto.getStars());
        assertEquals(20, dto.getViews());
        assertEquals("Alice", dto.getContributors().get(0).getName());

        verify(starRepository).findStarredPublicDiagramsByUser(7, PageRequest.of(0, 10));
        verify(userDiagramService).getContributors(diagramId);
    }

    /*
     * ----------------------------------------------------------
     * 6. star on missing diagram → throws
     * ----------------------------------------------------------
     */
    @Test
    void star_missingDiagram_throws() {

        when(userDiagramService.getDiagramOrThrow(diagramId))
                .thenThrow(new PublicDiagramException.DiagramNotFoundException("not found"));

        assertThrows(PublicDiagramException.DiagramNotFoundException.class,
                () -> service.starPublicDiagram(1, diagramId));
    }

    /*
     * ----------------------------------------------------------
     * 7. star on non-public diagram → throws
     * ----------------------------------------------------------
     */
    @Test
    void star_nonPublicDiagram_throws() {

        diagram.setPublicDiagram(null);

        when(userDiagramService.getDiagramOrThrow(diagramId))
                .thenReturn(diagram);

        assertThrows(PublicDiagramException.DiagramNotFoundException.class,
                () -> service.starPublicDiagram(2, diagramId));
    }
}
