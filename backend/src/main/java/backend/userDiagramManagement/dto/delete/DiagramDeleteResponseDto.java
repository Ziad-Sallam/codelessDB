package backend.userDiagramManagement.dto.delete;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

// to be deleted
@Data
@AllArgsConstructor
@Schema(description = "Response object for diagram deletion")
public class DiagramDeleteResponseDto {
    @Schema(description = "Result message", example = "Diagram deleted successfully")
    private String message;

    @Schema(description = "The ID of the deleted diagram", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID diagramId;
}
