package backend.publicDiagramManagement.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.entities.User;
import backend.entities.publicDiagramEntities.Comment;
import backend.entities.publicDiagramEntities.PublicDiagram;
import backend.publicDiagramManagement.dto.comment.CommentDto;
import backend.publicDiagramManagement.dto.comment.CommentRequestDto;
import backend.publicDiagramManagement.repository.CommentRepository;
import backend.publicDiagramManagement.repository.PublicDiagramRepository;
import backend.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PublicDiagramRepository publicDiagramRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(int userId, UUID diagramId) {
        List<Comment> allComments = commentRepository.findByPublicDiagramIdOrderByCreatedAtDesc(diagramId);
        return allComments.stream()
                .filter(c -> c.getParent() == null)
                .map(comment -> mapToDto(comment, userId))
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(int userId, UUID diagramId, CommentRequestDto commentRequestDto) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        PublicDiagram diagram = publicDiagramRepository.findById(diagramId)
                .orElseThrow(() -> new RuntimeException("Public Diagram not found"));

        Comment parent = null;
        if (commentRequestDto.parentId() != null) {
            parent = commentRepository.findById(commentRequestDto.parentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));

            // Check reply depth
            int depth = 0;
            Comment current = parent;
            while (current != null) {
                depth++;
                current = current.getParent();
                if (depth >= 3) {
                    throw new RuntimeException("Maximum reply depth reached. You can only reply up to 2 replays.");
                }
            }
        }

        Comment comment = Comment.builder()
                .content(commentRequestDto.content())
                .user(user)
                .publicDiagram(diagram)
                .parent(parent)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return mapToDto(savedComment, userId);
    }

    @Override
    @Transactional
    public CommentDto updateComment(int userId, Long commentId, CommentRequestDto commentRequestDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (comment.getUser().getId() != userId) {
            throw new RuntimeException("You are not allowed to edit this comment");
        }

        comment.setContent(commentRequestDto.content());
        Comment savedComment = commentRepository.save(comment);
        return mapToDto(savedComment, userId);
    }

    @Override
    @Transactional
    public void deleteComment(int userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (comment.getUser().getId() != userId) {
            throw new RuntimeException("You are not allowed to delete this comment");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public CommentDto reactToComment(int userId, Long commentId, String type) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if ("LIKE".equalsIgnoreCase(type)) {
            if (comment.getLikedBy().contains(user)) {
                comment.getLikedBy().remove(user);
            } else {
                comment.getDislikedBy().remove(user);
                comment.getLikedBy().add(user);
            }
        } else if ("DISLIKE".equalsIgnoreCase(type)) {
            if (comment.getDislikedBy().contains(user)) {
                comment.getDislikedBy().remove(user);
            } else {
                comment.getLikedBy().remove(user);
                comment.getDislikedBy().add(user);
            }
        } else {
            throw new RuntimeException("Invalid reaction type");
        }

        Comment savedComment = commentRepository.save(comment);
        return mapToDto(savedComment, userId);
    }

    private CommentDto mapToDto(Comment comment, int currentUserId) {
        String userReaction = null;
        if (comment.getLikedBy().stream().anyMatch(u -> u.getId() == currentUserId)) {
            userReaction = "LIKE";
        } else if (comment.getDislikedBy().stream().anyMatch(u -> u.getId() == currentUserId)) {
            userReaction = "DISLIKE";
        }

        return CommentDto.builder()
                .id(comment.getId())
                .username(comment.getUser().getUsername())
                .userPicture(comment.getUser().getPicture())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .owner(comment.getUser().getId() == currentUserId)
                .edited(comment.getUpdatedAt() != null && comment.getUpdatedAt().isAfter(comment.getCreatedAt()))
                .replies(comment.getReplies().stream()
                        .map(reply -> mapToDto(reply, currentUserId))
                        .toList())
                .likesCount(comment.getLikedBy().size())
                .dislikesCount(comment.getDislikedBy().size())
                .userReaction(userReaction)
                .build();
    }
}
