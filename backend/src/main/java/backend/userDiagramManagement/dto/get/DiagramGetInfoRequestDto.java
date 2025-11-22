package backend.userDiagramManagement.dto.get;

import lombok.Data;

@Data
public class DiagramGetInfoRequestDto {
    private int pageNumber = 0;
    private int pageSize = 10;
}
