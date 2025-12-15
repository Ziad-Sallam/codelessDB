package backend.publicDiagramManagement.dto.publish;

import backend.publicDiagramManagement.dto.DiagramCannedQueryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublishDiagramRequestDto {
    private UUID diagramId;
    private String shortDescription;
    private String detailedDescription;
    private List<String> hashTags;
    private List<DiagramCannedQueryDto> cannedQueries;
}
