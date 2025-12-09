package backend.collab.updateDto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DiagramUpdateResponseDto {
    private String message;
    private UUID diagramId;
    private Date updateDate;
}
