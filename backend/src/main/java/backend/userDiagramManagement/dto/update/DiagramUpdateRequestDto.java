package backend.userDiagramManagement.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request object for updating diagram name or content")
public class DiagramUpdateRequestDto {
    @Schema(description = "The new name of the diagram", example = "Modern E-commerce")
    private String name;
    @Schema(description = "The new binary or JSON content of the diagram")
    private byte[] jsonContent;
    @Schema(description = "The new thumbnail for the diagram", example = "https://example.com/new_thumb.png")
    private String thumbnail;
}