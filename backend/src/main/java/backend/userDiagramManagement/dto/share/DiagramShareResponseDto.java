package backend.userDiagramManagement.dto.share;

import backend.user.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiagramShareResponseDto {
    private String message;
    private String sharedWith;
    private Role role;
}
