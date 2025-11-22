package backend.userDiagramManagement.dto.create;

import backend.entities.Diagram;
import lombok.Data;

@Data
public class DiagramCreateRequestDto {
    private String name;
    private String jsonContent;
    private String thumbnail;

    public Diagram toDiagram() {
        return Diagram.builder()
                .name(this.name)
                .content(this.jsonContent)
                .thumbnail(this.thumbnail)
                .build();
    }
}

