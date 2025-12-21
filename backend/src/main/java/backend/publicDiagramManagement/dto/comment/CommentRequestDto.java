package backend.publicDiagramManagement.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequestDto(
        @NotBlank @Size(max = 1000) String content,
        Long parentId) {
}
