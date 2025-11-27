package backend.userDiagramManagement.dto.create;

import backend.entities.Diagram;
import lombok.Data;

@Data
public class DiagramCreateRequestDto {

    private String thumbnail;

    public Diagram toDiagram() {
        return Diagram.builder()
                .name("Untitled Diagram")
                .thumbnail(this.thumbnail)
                .build();
    }
}
