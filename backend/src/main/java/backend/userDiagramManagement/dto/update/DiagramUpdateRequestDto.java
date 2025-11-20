package backend.userDiagramManagement.dto.update;

import lombok.Data;

import java.util.UUID;

@Data
public class DiagramUpdateRequestDto {
    private UUID id;
    private String name;
    private String jsonContent;
}
