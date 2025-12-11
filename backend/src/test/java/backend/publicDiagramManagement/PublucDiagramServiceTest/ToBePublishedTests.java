package backend.publicDiagramManagement.PublucDiagramServiceTest;

import backend.entities.Diagram;
import backend.publicDiagramManagement.dto.get.ToBePublishedDiagramDto;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.publicDiagramManagement.service.PublicDiagramServiceImpl;
import backend.userDiagramManagement.dto.ContributorDto;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToBePublishedTests {

    @Mock private UserDiagramRepository userDiagramRepository;
    @Mock private UserDiagramService userDiagramService;
    @Mock private PublicDiagramRepository publicDiagramRepository;

    @InjectMocks
    private PublicDiagramServiceImpl service;

    private int userId;
    private UUID diagramId;
    private Diagram diagram;

    @BeforeEach
    void setup() {
        userId = 7;
        diagramId = UUID.randomUUID();

        diagram = Diagram.builder()
                .id(diagramId)
                .name("Draft")
                .ddl("CREATE TABLE X")
                .thumbnail("thumb.png")
                .build();
    }

    // --------------------------------------------------------
    // ✅ Test 1 — Empty page → returns empty DTO page
    // --------------------------------------------------------
    @Test
    void getToBePublished_emptyPage() {

        Page<Diagram> emptyPage = new PageImpl<>(Collections.emptyList());

        when(userDiagramRepository.findUnpublishedDiagramsByUser(eq(userId), any()))
                .thenReturn(emptyPage);

        Page<ToBePublishedDiagramDto> result =
                service.getToBePublishedDiagrams(userId, PageRequest.of(0, 10));

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userDiagramRepository, times(1))
                .findUnpublishedDiagramsByUser(eq(userId), any());
    }

    // --------------------------------------------------------
    // ✅ Test 2 — Single diagram → mapping is correct
    // --------------------------------------------------------
    @Test
    void getToBePublished_singleDiagram_mapsCorrectly() {

        Page<Diagram> inputPage = new PageImpl<>(List.of(diagram));

        when(userDiagramRepository.findUnpublishedDiagramsByUser(eq(userId), any()))
                .thenReturn(inputPage);

        List<ContributorDto> contributors = List.of(
                ContributorDto.builder()
                        .name("Owner")
                        .picture("pic.png")
                        .build()
        );

        when(userDiagramService.getContributors(diagramId))
                .thenReturn(contributors);

        Page<ToBePublishedDiagramDto> output =
                service.getToBePublishedDiagrams(userId, PageRequest.of(0, 10));

        assertEquals(1, output.getTotalElements());

        ToBePublishedDiagramDto dto = output.getContent().get(0);

        assertEquals(diagramId, dto.getDiagramId());
        assertEquals("Draft", dto.getName());
        assertEquals("CREATE TABLE X", dto.getDdl());
        assertEquals("thumb.png", dto.getThumbnail());
        assertEquals(contributors, dto.getContributors());

        verify(userDiagramService).getContributors(diagramId);
    }

    // --------------------------------------------------------
    // ✅ Test 3 — Multiple diagrams → mapping applied per element
    // --------------------------------------------------------
    @Test
    void getToBePublished_multipleDiagrams() {

        Diagram d2 = Diagram.builder()
                .id(UUID.randomUUID())
                .name("Second")
                .ddl("DDL2")
                .thumbnail("t2.png")
                .build();

        Page<Diagram> page = new PageImpl<>(List.of(diagram, d2));

        when(userDiagramRepository.findUnpublishedDiagramsByUser(eq(userId), any()))
                .thenReturn(page);

        when(userDiagramService.getContributors(any()))
                .thenReturn(Collections.emptyList());

        Page<ToBePublishedDiagramDto> result =
                service.getToBePublishedDiagrams(userId, PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());

        assertEquals("Draft", result.getContent().get(0).getName());
        assertEquals("Second", result.getContent().get(1).getName());

        verify(userDiagramService, times(2)).getContributors(any());
    }

    // --------------------------------------------------------
    // ✅ Test 4 — Pagination → page metadata preserved
    // --------------------------------------------------------
    @Test
    void getToBePublished_paginationPreserved() {

        PageRequest pageable = PageRequest.of(2, 5); // page 2, size 5

        Page<Diagram> page = new PageImpl<>(Collections.emptyList(), pageable, 20);

        when(userDiagramRepository.findUnpublishedDiagramsByUser(eq(userId), eq(pageable)))
                .thenReturn(page);

        Page<ToBePublishedDiagramDto> result =
                service.getToBePublishedDiagrams(userId, pageable);

        assertEquals(20, result.getTotalElements());
        assertEquals(4, result.getTotalPages());
        assertEquals(2, result.getNumber());

        verify(userDiagramRepository).findUnpublishedDiagramsByUser(userId, pageable);
    }

    // --------------------------------------------------------
    // ✅ Test 5 — Contributors service returns empty list → still safe
    // --------------------------------------------------------
    @Test
    void getToBePublished_noContributors() {

        Page<Diagram> page = new PageImpl<>(List.of(diagram));

        when(userDiagramRepository.findUnpublishedDiagramsByUser(eq(userId), any()))
                .thenReturn(page);

        when(userDiagramService.getContributors(diagramId))
                .thenReturn(Collections.emptyList());

        Page<ToBePublishedDiagramDto> result =
                service.getToBePublishedDiagrams(userId, PageRequest.of(0, 10));

        ToBePublishedDiagramDto dto = result.getContent().get(0);

        assertNotNull(dto.getContributors());
        assertTrue(dto.getContributors().isEmpty());
    }
}
