package backend.userDiagramManagement.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagramSearchRequestDto {
    private String name;   // partial or full diagram name
    private String start;  // "yyyy-MM-dd" format
    private String end;    // "yyyy-MM-dd" format
}