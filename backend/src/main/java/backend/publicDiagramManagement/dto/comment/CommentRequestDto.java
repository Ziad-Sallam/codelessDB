package backend.publicDiagramManagement.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for creating or updating a comment")
public record CommentRequestDto(
        @NotBlank @Size(max = 1000)
        @Schema(description = "The text content of the comment", example = "This is a great schema!")
        String content,

        @Schema(description = "The ID of the parent comment if this is a reply", example = "1")
        Long parentId) {
}
