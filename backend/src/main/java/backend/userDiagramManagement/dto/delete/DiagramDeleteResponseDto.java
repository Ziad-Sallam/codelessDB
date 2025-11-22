package backend.userDiagramManagement.dto.delete;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DiagramDeleteResponseDto {
    private String message;
    private UUID diagramId;
}
