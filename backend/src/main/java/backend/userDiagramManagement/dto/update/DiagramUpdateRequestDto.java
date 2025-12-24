package backend.userDiagramManagement.dto.update;

import lombok.Data;

@Data
public class DiagramUpdateRequestDto {
    private String name;
    private byte[] jsonContent;
    private String thumbnail;
}