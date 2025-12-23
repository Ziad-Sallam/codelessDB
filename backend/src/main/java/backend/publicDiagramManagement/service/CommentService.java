package backend.publicDiagramManagement.service;

import backend.publicDiagramManagement.dto.comment.CommentDto;
import backend.publicDiagramManagement.dto.comment.CommentRequestDto;
import java.util.List;
import java.util.UUID;

public interface CommentService {
    List<CommentDto> getComments(int userId, UUID diagramId);

    CommentDto addComment(int userId, UUID diagramId, CommentRequestDto commentRequestDto);

    CommentDto updateComment(int userId, Long commentId, CommentRequestDto commentRequestDto);

    void deleteComment(int userId, Long commentId);

    CommentDto reactToComment(int userId, Long commentId, String type);
}
