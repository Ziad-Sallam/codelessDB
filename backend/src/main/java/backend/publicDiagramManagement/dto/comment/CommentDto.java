package backend.publicDiagramManagement.dto.comment;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Data Transfer Object representing a comment on a public diagram")
public record CommentDto(
        @Schema(description = "The unique ID of the comment", example = "1")
        Long id,

        @Schema(description = "The username of the comment author", example = "johndoe")
        String username,

        @Schema(description = "URL of the author's profile picture", example = "https://example.com/pic.jpg")
        String userPicture,

        @Schema(description = "The text content of the comment", example = "Great schema!")
        String content,

        @Schema(description = "The timestamp when the comment was created")
        LocalDateTime createdAt,

        @Schema(description = "Whether the current user is the owner of this comment", example = "false")
        boolean owner,

        @Schema(description = "Whether the comment has been edited", example = "false")
        boolean edited,

        @Schema(description = "List of replies to this comment")
        java.util.List<CommentDto> replies,

        @Schema(description = "Number of likes for this comment", example = "5")
        int likesCount,

        @Schema(description = "Number of dislikes for this comment", example = "1")
        int dislikesCount,

        @Schema(description = "The reaction of the current user to this comment (LIKE, DISLIKE, or null)", example = "LIKE")
        String userReaction) {
}
