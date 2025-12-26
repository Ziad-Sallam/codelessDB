package backend.userDiagramManagement.dto.create;

import backend.entities.Diagram;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request object for creating a new database diagram")
public class DiagramCreateRequestDto {

    @Schema(description = "URL or base64 of the diagram thumbnail", example = "https://example.com/thumbnail.png")
    private String thumbnail;

    public Diagram toDiagram() {
        return Diagram.builder()
                      .name("Untitled Diagram")
                      .thumbnail(this.thumbnail)
                      .build();
    }
}
