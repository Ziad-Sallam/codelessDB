package backend.publicDiagramManagement.PublucDiagramServiceTest;

import backend.entities.Diagram;
import backend.entities.User;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.dto.PublicDiagramInfoDto;
import backend.publicDiagramManagement.dto.search.SearchRequestDto;
import backend.publicDiagramManagement.dto.user.PublicUserInfoDto;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.publicDiagramManagement.service.PublicDiagramServiceImpl;
import backend.user.UserService;
import backend.userDiagramManagement.dto.ContributorDto;
import backend.userDiagramManagement.service.UserDiagramService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.UUID;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PublicDiagramServiceSearchTests {

    @Mock
    private PublicDiagramRepository publicDiagramRepository;
    @Mock
    private UserDiagramService userDiagramService;
    @Mock
    private UserService userService;
    @Mock
    private backend.user.UserRepository userRepository;

    @InjectMocks
    private PublicDiagramServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // --------------------------
    // searchPublicDiagrams tests
    // --------------------------
    @Test
    void searchPublicDiagrams_returnsDtos() {
        // Arrange
        PublicDiagram pd = new PublicDiagram();
        pd.setId(UUID.randomUUID());

        // Set inner Diagram to avoid NPE
        Diagram diagram = new Diagram();
        diagram.setName("Test Diagram");
        pd.setDiagram(diagram);

        Pageable pageable = PageRequest.of(0, 10);
        Page<PublicDiagram> page = new PageImpl<>(List.of(pd), pageable, 1);

        when(publicDiagramRepository.searchPublicDiagrams("test", null, pageable))
                .thenReturn(page);
        when(userDiagramService.getContributors(pd.getId()))
                .thenReturn(List.of(new ContributorDto("Alice", "pic", null)));

        SearchRequestDto dto = new SearchRequestDto();
        dto.setSearchPrompt("test");
        dto.setHashtags(null); // null tags

        // Act
        Page<PublicDiagramInfoDto> result = service.searchPublicDiagrams(dto, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        PublicDiagramInfoDto dtoResult = result.getContent().get(0);

        assertEquals(pd.getId(), dtoResult.getDiagramId());
        assertEquals("Test Diagram", dtoResult.getName()); // Check diagram name
        assertEquals(1, dtoResult.getContributors().size());
        assertEquals("Alice", dtoResult.getContributors().get(0).getName());

        // Verify repository and service calls
        verify(publicDiagramRepository).searchPublicDiagrams("test", null, pageable);
        verify(userDiagramService).getContributors(pd.getId());
    }

    @Test
    void searchPublicDiagrams_emptyTags_setsNull() {
        // Arrange
        PublicDiagram pd = new PublicDiagram();
        pd.setId(UUID.randomUUID());

        // Set inner Diagram to avoid NPE
        Diagram diagram = new Diagram();
        diagram.setName("Empty Tags Diagram");
        pd.setDiagram(diagram);

        Pageable pageable = PageRequest.of(0, 10);
        Page<PublicDiagram> page = new PageImpl<>(List.of(pd), pageable, 1);

        when(publicDiagramRepository.searchPublicDiagrams("search", null, pageable))
                .thenReturn(page);
        when(userDiagramService.getContributors(pd.getId()))
                .thenReturn(List.of()); // no contributors

        SearchRequestDto dto = new SearchRequestDto();
        dto.setSearchPrompt("search");
        dto.setHashtags(List.of()); // empty list

        // Act
        Page<PublicDiagramInfoDto> result = service.searchPublicDiagrams(dto, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        PublicDiagramInfoDto dtoResult = result.getContent().get(0);

        assertEquals(pd.getId(), dtoResult.getDiagramId());
        assertEquals("Empty Tags Diagram", dtoResult.getName()); // Check diagram name
        assertEquals(0, dtoResult.getContributors().size());

        // Verify repository and service calls
        verify(publicDiagramRepository).searchPublicDiagrams("search", null, pageable);
        verify(userDiagramService).getContributors(pd.getId());
    }

    // -------------------------------
    // searchUsersByPublicDiagrams tests
    // -------------------------------
    @Test
    void searchUsersByPublicDiagrams_returnsDtos() {
        User user = new User();
        user.setId(1);
        Object[] row = new Object[] { user, 5L, 10L, 20L };

        Pageable pageable = PageRequest.of(0, 10);
        Page<Object[]> page = new PageImpl<>(Collections.singletonList(row), pageable, 1);

        when(userRepository.searchUsersWithPublicStats("search", List.of("tag1"), pageable))
                .thenReturn(page);

        SearchRequestDto dto = new SearchRequestDto();
        dto.setSearchPrompt("search");
        dto.setHashtags(List.of("tag1"));

        Page<PublicUserInfoDto> result = service.searchUsersByPublicDiagrams(dto, pageable);

        assertEquals(1, result.getTotalElements());
        PublicUserInfoDto dtoResult = result.getContent().get(0);
        assertEquals(1L, dtoResult.getId());
        assertEquals(5L, dtoResult.getPublicCount());
        assertEquals(10L, dtoResult.getTotalStars());
        assertEquals(20L, dtoResult.getScore());

        verify(userRepository).searchUsersWithPublicStats("search", List.of("tag1"), pageable);
    }

    @Test
    void searchUsersByPublicDiagrams_blankSearchAndEmptyTags_setsNull() {
        User user = new User();
        user.setId(2);
        Object[] row = new Object[] { user, null, null, null };

        Pageable pageable = PageRequest.of(0, 10);
        Page<Object[]> page = new PageImpl<>(Collections.singletonList(row), pageable, 1);

        when(userRepository.searchUsersWithPublicStats(null, null, pageable))
                .thenReturn(page);

        SearchRequestDto dto = new SearchRequestDto();
        dto.setSearchPrompt("  "); // blank
        dto.setHashtags(List.of()); // empty list

        Page<PublicUserInfoDto> result = service.searchUsersByPublicDiagrams(dto, pageable);

        assertEquals(1, result.getTotalElements());
        PublicUserInfoDto dtoResult = result.getContent().get(0);
        assertEquals(2L, dtoResult.getId());
        assertEquals(0L, dtoResult.getPublicCount());
        assertEquals(0L, dtoResult.getTotalStars());
        assertEquals(0L, dtoResult.getScore());

        verify(userRepository).searchUsersWithPublicStats(null, null, pageable);
    }
}
