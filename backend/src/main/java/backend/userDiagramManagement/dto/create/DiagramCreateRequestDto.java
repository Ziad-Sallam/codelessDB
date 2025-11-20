package backend.userDiagramManagement.dto.create;

import lombok.Data;

@Data
public class DiagramCreateRequestDto {
    private String name;
    private String jsonContent;
    private byte[] thumbnail;
}
