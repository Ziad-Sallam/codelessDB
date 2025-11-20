package backend.userDiagramManagement.dto.share;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiagramShareResponseDto {
    private String message;
    private String sharedWith;
    private String role;
}
