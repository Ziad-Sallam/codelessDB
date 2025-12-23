package backend.publicDiagramManagement.dto.comment;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record CommentDto(
        Long id,
        String username,
        String userPicture,
        String content,
        LocalDateTime createdAt,
        boolean owner,
        boolean edited,
        java.util.List<CommentDto> replies,
        int likesCount,
        int dislikesCount,
        String userReaction) {
}
