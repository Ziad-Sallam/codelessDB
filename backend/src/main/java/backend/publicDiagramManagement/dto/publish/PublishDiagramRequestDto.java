package backend.publicDiagramManagement.dto.publish;

import java.util.List;
import java.util.UUID;

import backend.publicDiagramManagement.dto.DiagramCannedQueryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for publishing a diagram to the public gallery")
public class PublishDiagramRequestDto {
    @Schema(description = "The unique ID of the diagram to be published", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID diagramId;

    @Schema(description = "A short description for the public gallery", example = "E-commerce DB schema")
    private String shortDescription;

    @Schema(description = "A detailed description of the diagram", example = "This schema covers the core entities...")
    private String detailedDescription;

    @Schema(description = "List of hashtags for categorization")
    private List<String> hashTags;

    @Schema(description = "List of canned queries to include with the public diagram")
    private List<DiagramCannedQueryDto> cannedQueries;
}
