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
import backend.publicDiagramManagement.dto.user.PublicUserFollowDto;
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

        PublicUserDto dto = service.getDesignerProfile(null, "john");

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

        assertThatThrownBy(() -> service.getDesignerProfile(null, "missing"))
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

        assertThat(result.getContent().get(0).getStars()).isEqualTo(9);
    }

    @Test
    void testFollowUser_success() {
        User me = User.builder().id(1).username("me").build();
        User target = User.builder().id(2).username("target").build();

        when(userRepository.findById(1)).thenReturn(me);
        when(userRepository.findByUsername("target")).thenReturn(target);
        when(userRepository.countFollowing(1, 2)).thenReturn(0L);

        service.followUser(1, "target");

        // Verify native inserts are called
        verify(userRepository).addFollower(2, 1);
        verify(userRepository).addFollowing(1, 2);
    }

    @Test
    void testFollowUser_alreadyFollowing() {
        User me = User.builder().id(1).username("me").build();
        User target = User.builder().id(2).username("target").build();

        when(userRepository.findById(1)).thenReturn(me);
        when(userRepository.findByUsername("target")).thenReturn(target);
        when(userRepository.countFollowing(1, 2)).thenReturn(1L);

        assertThatThrownBy(() -> service.followUser(1, "target"))
                .isInstanceOf(UserException.UserAlreadyFollowedException.class)
                .hasMessageContaining("is already following");

        // Verify native inserts are NOT called
        verify(userRepository, never()).addFollowing(anyInt(), anyInt());
    }


    @Test
    void testUnfollowUser_success() {
        User me = User.builder().id(1).username("me").build();
        User target = User.builder().id(2).username("target").build();

        when(userRepository.findById(1)).thenReturn(me);
        when(userRepository.findByUsername("target")).thenReturn(target);
        when(userRepository.countFollowing(1, 2)).thenReturn(1L);

        service.unfollowUser(1, "target");

        // Verify native deletes are called
        verify(userRepository).removeFollowing(1, 2);
        verify(userRepository).removeFollower(2, 1);
    }

    @Test
    void testUnfollowUser_notFollowing() {
        User me = User.builder().id(1).username("me").build();
        User target = User.builder().id(2).username("target").build();

        when(userRepository.findById(1)).thenReturn(me);
        when(userRepository.findByUsername("target")).thenReturn(target);
        when(userRepository.countFollowing(1, 2)).thenReturn(0L);

        assertThatThrownBy(() -> service.unfollowUser(1, "target"))
                .isInstanceOf(UserException.UserAlreadyFollowedException.class)
                .hasMessageContaining("is not following");

        verify(userRepository, never()).removeFollowing(anyInt(), anyInt());
    }
    @Test
    void testGetFollowersByUsername_success() {
        User target = User.builder().id(1).username("john").build();
        User follower = User.builder().id(2).username("follower").build();
        Object[] row = new Object[]{follower, 5L, 10L, 100L};
        Page<Object[]> page = new PageImpl<Object[]>(List.<Object[]>of(row));

        when(userRepository.findByUsername("john")).thenReturn(target);
        when(userRepository.findFollowersWithStats(eq(1), any())).thenReturn(page);

        Page<PublicUserFollowDto> result = service.getFollowersByUsername("john", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("follower");
    }

    @Test
    void testGetFollowersByUsername_userNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(null);

        assertThatThrownBy(() -> service.getFollowersByUsername("missing", PageRequest.of(0, 10)))
                .isInstanceOf(UserException.UserNotFoundException.class);
    }

    @Test
    void testGetFollowersByUsername_emptyResults() {
        User target = User.builder().id(1).username("john").build();
        Page<Object[]> page = new PageImpl<Object[]>(List.of());

        when(userRepository.findByUsername("john")).thenReturn(target);
        when(userRepository.findFollowersWithStats(eq(1), any())).thenReturn(page);

        Page<PublicUserFollowDto> result = service.getFollowersByUsername("john", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void testGetFollowingsByUsername_success() {
        User target = User.builder().id(1).username("john").build();
        User followed = User.builder().id(2).username("followed").build();
        Object[] row = new Object[]{followed, 3L, 8L, 50L};
        Page<Object[]> page = new PageImpl<Object[]>(List.<Object[]>of(row));

        when(userRepository.findByUsername("john")).thenReturn(target);
        when(userRepository.findFollowingWithStats(eq(1), any())).thenReturn(page);

        Page<PublicUserFollowDto> result = service.getFollowingsByUsername("john", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("followed");
    }

    @Test
    void testGetFollowingsByUsername_userNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(null);

        assertThatThrownBy(() -> service.getFollowingsByUsername("missing", PageRequest.of(0, 10)))
                .isInstanceOf(UserException.UserNotFoundException.class);
    }

    @Test
    void testGetFollowingsByUsername_emptyResults() {
        User target = User.builder().id(1).username("john").build();
        Page<Object[]> page = new PageImpl<Object[]>(List.of());

        when(userRepository.findByUsername("john")).thenReturn(target);
        when(userRepository.findFollowingWithStats(eq(1), any())).thenReturn(page);

        Page<PublicUserFollowDto> result = service.getFollowingsByUsername("john", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void testGetFollowers_success() {
        User follower = User.builder().id(2).username("follower").build();
        Object[] row = new Object[]{follower, 5L, 10L, 100L};
        Page<Object[]> page = new PageImpl<Object[]>(List.<Object[]>of(row));

        when(userRepository.findFollowersWithStats(eq(1), any())).thenReturn(page);

        Page<PublicUserFollowDto> result = service.getFollowers(1, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("follower");
        assertThat(result.getContent().get(0).getPublicCount()).isEqualTo(5L);
    }

    @Test
    void testGetFollowings_success() {
        User followed = User.builder().id(2).username("followed").build();
        Object[] row = new Object[]{followed, 3L, 8L, 50L};
        Page<Object[]> page = new PageImpl<Object[]>(List.<Object[]>of(row));

        when(userRepository.findFollowingWithStats(eq(1), any())).thenReturn(page);

        Page<PublicUserFollowDto> result = service.getFollowings(1, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("followed");
        assertThat(result.getContent().get(0).getTotalStars()).isEqualTo(8L);
    }

    @Test
    void testGetFollowers_nullStats() {
        User follower = User.builder().id(2).username("follower").build();
        Object[] row = new Object[]{follower, null, null, null};
        Page<Object[]> page = new PageImpl<Object[]>(List.<Object[]>of(row));

        when(userRepository.findFollowersWithStats(eq(1), any())).thenReturn(page);

        Page<PublicUserFollowDto> result = service.getFollowers(1, PageRequest.of(0, 10));

        assertThat(result.getContent().get(0).getPublicCount()).isEqualTo(0L);
        assertThat(result.getContent().get(0).getTotalStars()).isEqualTo(0L);
    }
}
