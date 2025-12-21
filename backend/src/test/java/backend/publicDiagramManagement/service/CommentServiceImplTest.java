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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.entities.User;
import backend.entities.publicDiagramEntities.Comment;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.dto.comment.CommentDto;
import backend.publicDiagramManagement.dto.comment.CommentRequestDto;
import backend.publicDiagramManagement.repository.CommentRepository;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.user.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {

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
    void setUp() {
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
    void getComments_Success() {
        Comment comment = Comment.builder()
                .id(1L)
                .content("Test Comment")
                .user(testUser)
                .publicDiagram(testDiagram)
                .createdAt(LocalDateTime.now())
                .likedBy(new HashSet<>())
                .dislikedBy(new HashSet<>())
                .replies(new ArrayList<>())
                .build();

        when(commentRepository.findByPublicDiagramIdOrderByCreatedAtDesc(diagramId))
                .thenReturn(List.of(comment));

        List<CommentDto> result = commentService.getComments(1, diagramId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Comment", result.get(0).content());
        assertEquals("testuser", result.get(0).username());
    }

    @Test
    void addComment_Success() {
        CommentRequestDto requestDto = new CommentRequestDto("New Comment", null);

        when(userRepository.findById(1)).thenReturn(testUser);
        when(publicDiagramRepository.findById(diagramId)).thenReturn(Optional.of(testDiagram));

        Comment savedComment = Comment.builder()
                .id(1L)
                .content("New Comment")
                .user(testUser)
                .publicDiagram(testDiagram)
                .createdAt(LocalDateTime.now())
                .likedBy(new HashSet<>())
                .dislikedBy(new HashSet<>())
                .replies(new ArrayList<>())
                .build();

        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentDto result = commentService.addComment(1, diagramId, requestDto);

        assertNotNull(result);
        assertEquals("New Comment", result.content());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_UserNotFound() {
        CommentRequestDto requestDto = new CommentRequestDto("New Comment", null);
        when(userRepository.findById(1)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> commentService.addComment(1, diagramId, requestDto));
    }

    @Test
    void updateComment_Success() {
        CommentRequestDto requestDto = new CommentRequestDto("Updated Content", null);
        Comment existingComment = Comment.builder()
                .id(1L)
                .content("Old Content")
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .likedBy(new HashSet<>())
                .dislikedBy(new HashSet<>())
                .replies(new ArrayList<>())
                .build();

        when(commentRepository.findById(1L)).thenReturn(Optional.of(existingComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(existingComment);

        CommentDto result = commentService.updateComment(1, 1L, requestDto);

        assertNotNull(result);
        assertEquals("Updated Content", result.content());
    }

    @Test
    void updateComment_NotOwner() {
        CommentRequestDto requestDto = new CommentRequestDto("Updated Content", null);
        User otherUser = User.builder().id(2).build();
        Comment existingComment = Comment.builder()
                .id(1L)
                .user(otherUser)
                .build();

        when(commentRepository.findById(1L)).thenReturn(Optional.of(existingComment));

        assertThrows(RuntimeException.class, () -> commentService.updateComment(1, 1L, requestDto));
    }

    @Test
    void deleteComment_Success() {
        Comment existingComment = Comment.builder()
                .id(1L)
                .user(testUser)
                .build();

        when(commentRepository.findById(1L)).thenReturn(Optional.of(existingComment));

        commentService.deleteComment(1, 1L);

        verify(commentRepository).delete(existingComment);
    }

    @Test
    void reactToComment_Like() {
        Comment comment = Comment.builder()
                .id(1L)
                .user(testUser)
                .likedBy(new HashSet<>())
                .dislikedBy(new HashSet<>())
                .replies(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(1)).thenReturn(testUser);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = commentService.reactToComment(1, 1L, "LIKE");

        assertNotNull(result);
        assertEquals(1, result.likesCount());
        assertEquals("LIKE", result.userReaction());
        assertTrue(comment.getLikedBy().contains(testUser));
    }

    @Test
    void reactToComment_ToggleLike() {
        HashSet<User> likedBy = new HashSet<>();
        likedBy.add(testUser);
        Comment comment = Comment.builder()
                .id(1L)
                .user(testUser)
                .likedBy(likedBy)
                .dislikedBy(new HashSet<>())
                .replies(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(1)).thenReturn(testUser);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = commentService.reactToComment(1, 1L, "LIKE");

        assertEquals(0, result.likesCount());
        assertNull(result.userReaction());
        assertFalse(comment.getLikedBy().contains(testUser));
    }
}
