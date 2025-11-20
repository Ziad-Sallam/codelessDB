package backend.userDiagramManagement.dto.search;

import lombok.Data;

@Data
public class DiagramSearchRequestDto {
    private String name;
    private String start;
    private String end;
}
