package backend.publicDiagramManagement;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import backend.entities.User;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.dto.PublicDiagramInfoDto;
import backend.publicDiagramManagement.dto.user.PublicUserDto;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.publicDiagramManagement.service.PublicUserService;
import backend.user.Role;
import backend.user.UserRepository;
import backend.user.exceptions.UserException;
import backend.userDiagramManagement.dto.ContributorDto;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.service.UserDiagramService;

public class PublicUserServiceTest {

    UserRepository userRepository;
    DiagramRepository diagramRepository;
    PublicDiagramRepository publicDiagramRepository;
    UserDiagramService userDiagramService;

    PublicUserService service;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        diagramRepository = mock(DiagramRepository.class);
        publicDiagramRepository = mock(PublicDiagramRepository.class);
        userDiagramService = mock(UserDiagramService.class);

        service = new PublicUserService(
                userRepository,
                diagramRepository,
                publicDiagramRepository,
                userDiagramService);
    }

    // ----------------------------------------------------
    // ✅ getDesignerProfile – success
    // ----------------------------------------------------
    @Test
    void testGetDesignerProfile_success() {
        User u = User.builder()
                .id(1)
                .username("john")
                .email("john@mail.com")
                .bio("bio")
                .picture("pic")
                .profileWebsiteUrl("url")
                .publicProfile("publicName")
                .totalStars(10L)
                .build();

        when(userRepository.findByUsername("john")).thenReturn(u);
        when(diagramRepository.countPublicDiagramsByOwner(1)).thenReturn(3L);

        PublicUserDto dto = service.getDesignerProfile("john");

        assertThat(dto.getUsername()).isEqualTo("john");
        assertThat(dto.getPublicCount()).isEqualTo(3L);
        assertThat(dto.getTotalStars()).isEqualTo(10L);
    }

    // ----------------------------------------------------
    // ❌ getDesignerProfile – user not found
    // ----------------------------------------------------
    @Test
    void testGetDesignerProfile_userNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(null);

        assertThatThrownBy(() -> service.getDesignerProfile("missing"))
                .isInstanceOf(UserException.UserNotFoundException.class);
    }

    // ----------------------------------------------------
    // ✅ getPublicDiagrams
    // ----------------------------------------------------
    @Test
    void testGetPublicDiagrams() {

        PublicDiagram pd = PublicDiagram.builder()
                .id(UUID.randomUUID())
                .diagram(
                        backend.entities.Diagram.builder()
                                .id(UUID.randomUUID())
                                .name("D1")
                                .thumbnail("t")
                                .createdAt(LocalDateTime.now())
                                .lastModified(LocalDateTime.now())
                                .build())
                .shortDescription("short")
                .stars(5)
                .forks(2)
                .views(8)
                .build();

        Page<PublicDiagram> page = new PageImpl<>(List.of(pd));

        when(publicDiagramRepository.findPublicDiagramsByOwner(eq("john"), any()))
                .thenReturn(page);

        when(userDiagramService.getContributors(pd.getId()))
                .thenReturn(List.of(new ContributorDto("john", "aasd", Role.OWNER)));

        Page<PublicDiagramInfoDto> result = service.getPublicDiagrams("john", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("D1");
        assertThat(result.getContent().get(0).getContributors()).hasSize(1);
    }

    // ----------------------------------------------------
    // ⭐ getStaredPublicDiagrams
    // ----------------------------------------------------
    @Test
    void testGetStaredPublicDiagrams() {

        PublicDiagram pd = PublicDiagram.builder()
                .id(UUID.randomUUID())
                .diagram(
                        backend.entities.Diagram.builder()
                                .id(UUID.randomUUID())
                                .name("Starred")
                                .thumbnail("t2")
                                .createdAt(LocalDateTime.now())
                                .lastModified(LocalDateTime.now())
                                .build())
                .shortDescription("s2")
                .stars(9)
                .forks(1)
                .views(15)
                .build();

        Page<PublicDiagram> page = new PageImpl<>(List.of(pd));

        when(publicDiagramRepository.findStaredPublicDiagramsByUser(eq("john"), any()))
                .thenReturn(page);

        when(userDiagramService.getContributors(pd.getId()))
                .thenReturn(List.of(new ContributorDto("john", "aasd", Role.OWNER)));

        Page<PublicDiagramInfoDto> result = service.getStaredPublicDiagrams("john", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getStars()).isEqualTo(9);
    }
}
