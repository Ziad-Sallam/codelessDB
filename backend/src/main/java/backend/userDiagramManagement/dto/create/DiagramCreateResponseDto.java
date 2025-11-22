package backend.userDiagramManagement.dto.create;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DiagramCreateResponseDto {
    private String message;
    private UUID diagramId;
}
