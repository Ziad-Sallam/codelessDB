package backend.userDiagramManagement.dto.create;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DiagramCreateResponseDto {
    private UUID diagramId;
    private Date createdAt;
    private Date lastModified;
}
