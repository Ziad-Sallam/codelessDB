package backend.publicDiagramManagement.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import backend.entities.User;
import backend.entities.publicDiagramEntities.Comment;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.dto.comment.CommentDto;
import backend.publicDiagramManagement.dto.comment.CommentRequestDto;
import backend.publicDiagramManagement.repository.CommentRepository;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.user.UserRepository;

class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PublicDiagramRepository publicDiagramRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User testUser;
    private PublicDiagram testDiagram;
    private UUID diagramId;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        diagramId = UUID.randomUUID();
        testUser = User.builder()
                .id(1)
                .username("testuser")
                .picture("pic_url")
                .build();
        testDiagram = PublicDiagram.builder()
                .id(diagramId)
                .build();
    }

    @Test
    void getComments_success() {
        Comment parent = Comment.builder()
                .id(1L)
                .content("Parent")
                .user(testUser)
                .publicDiagram(testDiagram)
                .createdAt(LocalDateTime.now())
                .likedBy(new HashSet<>())
                .dislikedBy(new HashSet<>())
                .replies(new ArrayList<>())
                .build();

        Comment reply = Comment.builder()
                .id(2L)
                .content("Reply")
                .user(testUser)
                .publicDiagram(testDiagram)
                .parent(parent)
                .createdAt(LocalDateTime.now())
                .likedBy(new HashSet<>())
                .dislikedBy(new HashSet<>())
                .replies(new ArrayList<>())
                .build();

        parent.getReplies().add(reply);

        when(commentRepository.findByPublicDiagramIdOrderByCreatedAtDesc(diagramId))
                .thenReturn(List.of(parent, reply));

        List<CommentDto> result = commentService.getComments(1, diagramId);

        assertEquals(1, result.size());
        assertEquals("Parent", result.get(0).content());
        assertEquals(1, result.get(0).replies().size());
        assertEquals("Reply", result.get(0).replies().get(0).content());
    }

    @Test
    void addComment_success_topLevel() {
        CommentRequestDto dto = new CommentRequestDto("Top", null);
        when(userRepository.findById(1)).thenReturn(testUser);
        when(publicDiagramRepository.findById(diagramId)).thenReturn(Optional.of(testDiagram));

        Comment saved = Comment.builder()
                .id(1L)
                .content("Top")
                .user(testUser)
                .publicDiagram(testDiagram)
                .createdAt(LocalDateTime.now())
                .likedBy(new HashSet<>())
                .dislikedBy(new HashSet<>())
                .replies(new ArrayList<>())
                .build();
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        CommentDto result = commentService.addComment(1, diagramId, dto);

        assertNotNull(result);
        assertEquals("Top", result.content());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_success_reply() {
        Comment parent = Comment.builder().id(10L).user(testUser).build();
        CommentRequestDto dto = new CommentRequestDto("Reply", 10L);
        when(userRepository.findById(1)).thenReturn(testUser);
        when(publicDiagramRepository.findById(diagramId)).thenReturn(Optional.of(testDiagram));
        when(commentRepository.findById(10L)).thenReturn(Optional.of(parent));

        Comment saved = Comment.builder()
                .id(11L)
                .content("Reply")
                .user(testUser)
                .publicDiagram(testDiagram)
                .parent(parent)
                .createdAt(LocalDateTime.now())
                .likedBy(new HashSet<>())
                .dislikedBy(new HashSet<>())
                .replies(new ArrayList<>())
                .build();
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        CommentDto result = commentService.addComment(1, diagramId, dto);

        assertEquals("Reply", result.content());
        verify(commentRepository).save(argThat(c -> c.getParent().getId() == 10L));
    }

    @Test
    void addComment_userNotFound() {
        when(userRepository.findById(1)).thenReturn(null);
        assertThrows(RuntimeException.class,
                () -> commentService.addComment(1, diagramId, new CommentRequestDto("test", null)));
    }

    @Test
    void addComment_diagramNotFound() {
        when(userRepository.findById(1)).thenReturn(testUser);
        when(publicDiagramRepository.findById(diagramId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> commentService.addComment(1, diagramId, new CommentRequestDto("test", null)));
    }

    @Test
    void addComment_parentNotFound() {
        when(userRepository.findById(1)).thenReturn(testUser);
        when(publicDiagramRepository.findById(diagramId)).thenReturn(Optional.of(testDiagram));
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        CommentRequestDto dto = new CommentRequestDto("test", 99L);
        assertThrows(RuntimeException.class, () -> commentService.addComment(1, diagramId, dto));
    }

    @Test
    void updateComment_success() {
        Comment existing = Comment.builder()
                .id(1L)
                .content("Old")
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .likedBy(new HashSet<>())
                .dislikedBy(new HashSet<>())
                .replies(new ArrayList<>())
                .build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArgument(0));

        CommentRequestDto dto = new CommentRequestDto("New", null);
        CommentDto result = commentService.updateComment(1, 1L, dto);

        assertEquals("New", result.content());
    }

    @Test
    void updateComment_notFound() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> commentService.updateComment(1, 1L, new CommentRequestDto("new", null)));
    }

    @Test
    void updateComment_notOwner() {
        User other = User.builder().id(2).build();
        Comment existing = Comment.builder().id(1L).user(other).build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class,
                () -> commentService.updateComment(1, 1L, new CommentRequestDto("new", null)));
    }

    @Test
    void deleteComment_success() {
        Comment existing = Comment.builder().id(1L).user(testUser).build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(existing));

        commentService.deleteComment(1, 1L);
        verify(commentRepository).delete(existing);
    }

    @Test
    void deleteComment_notOwner() {
        User other = User.builder().id(2).build();
        Comment existing = Comment.builder().id(1L).user(other).build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class, () -> commentService.deleteComment(1, 1L));
    }

    @Test
    void reactToComment_like_toggleOn() {
        Comment comment = createComment(1L);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(1)).thenReturn(testUser);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = commentService.reactToComment(1, 1L, "LIKE");

        assertTrue(comment.getLikedBy().contains(testUser));
        assertFalse(comment.getDislikedBy().contains(testUser));
        assertEquals("LIKE", result.userReaction());
    }

    @Test
    void reactToComment_like_toggleOff() {
        Comment comment = createComment(1L);
        comment.getLikedBy().add(testUser);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(1)).thenReturn(testUser);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = commentService.reactToComment(1, 1L, "LIKE");

        assertFalse(comment.getLikedBy().contains(testUser));
        assertNull(result.userReaction());
    }

    @Test
    void reactToComment_dislike_switchToLike() {
        Comment comment = createComment(1L);
        comment.getDislikedBy().add(testUser);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(1)).thenReturn(testUser);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = commentService.reactToComment(1, 1L, "LIKE");

        assertTrue(comment.getLikedBy().contains(testUser));
        assertFalse(comment.getDislikedBy().contains(testUser));
        assertEquals("LIKE", result.userReaction());
    }

    @Test
    void reactToComment_dislike_toggleOn() {
        Comment comment = createComment(1L);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(1)).thenReturn(testUser);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = commentService.reactToComment(1, 1L, "DISLIKE");

        assertTrue(comment.getDislikedBy().contains(testUser));
        assertEquals("DISLIKE", result.userReaction());
    }

    @Test
    void reactToComment_dislike_toggleOff() {
        Comment comment = createComment(1L);
        comment.getDislikedBy().add(testUser);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(1)).thenReturn(testUser);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        commentService.reactToComment(1, 1L, "DISLIKE");

        assertFalse(comment.getDislikedBy().contains(testUser));
    }

    @Test
    void reactToComment_like_switchToDislike() {
        Comment comment = createComment(1L);
        comment.getLikedBy().add(testUser);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(1)).thenReturn(testUser);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        commentService.reactToComment(1, 1L, "DISLIKE");

        assertTrue(comment.getDislikedBy().contains(testUser));
        assertFalse(comment.getLikedBy().contains(testUser));
    }

    @Test
    void reactToComment_invalidType() {
        Comment comment = createComment(1L);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(1)).thenReturn(testUser);

        assertThrows(RuntimeException.class, () -> commentService.reactToComment(1, 1L, "HATE"));
    }

    @Test
    void mapToDto_edited() {
        Comment comment = createComment(1L);
        comment.setCreatedAt(LocalDateTime.now().minusHours(1));
        comment.setUpdatedAt(LocalDateTime.now());
        when(commentRepository.findByPublicDiagramIdOrderByCreatedAtDesc(diagramId)).thenReturn(List.of(comment));

        CommentDto result = commentService.getComments(1, diagramId).get(0);
        assertTrue(result.edited());
    }

    @Test
    void mapToDto_userReactionDislike() {
        Comment comment = createComment(1L);
        comment.getDislikedBy().add(testUser);
        when(commentRepository.findByPublicDiagramIdOrderByCreatedAtDesc(diagramId)).thenReturn(List.of(comment));

        CommentDto result = commentService.getComments(1, diagramId).get(0);
        assertEquals("DISLIKE", result.userReaction());
    }

    private Comment createComment(Long id) {
        return Comment.builder()
                .id(id)
                .content("Content")
                .user(testUser)
                .publicDiagram(testDiagram)
                .createdAt(LocalDateTime.now())
                .likedBy(new HashSet<>())
                .dislikedBy(new HashSet<>())
                .replies(new ArrayList<>())
                .build();
    }
}
