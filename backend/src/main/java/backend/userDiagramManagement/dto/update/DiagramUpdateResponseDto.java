package backend.userDiagramManagement.dto.update;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Response object for diagram update result")
public class DiagramUpdateResponseDto {
    @Schema(description = "Result message", example = "Diagram updated successfully")
    private String message;

    @Schema(description = "The ID of the updated diagram", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID diagramId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "The timestamp when the update occurred")
    private LocalDateTime updateDate;
}
