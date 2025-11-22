package backend.userDiagramManagement.dto.share;

import backend.user.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class DiagramShareRequestDto {
    private UUID diagramId;
    private String toUserName;
    private Role role;
}
