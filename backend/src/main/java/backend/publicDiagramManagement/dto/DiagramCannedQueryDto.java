package backend.publicDiagramManagement.dto;

import backend.entities.publicDiagramEntities.CannedQueriesDiagrams;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiagramCannedQueryDto {
    private String name;
    private String description;
    private String query;
    private int id;

    public static DiagramCannedQueryDto toDto(CannedQueriesDiagrams cannedQueriesDiagrams) {
        return DiagramCannedQueryDto.builder()
                .id(cannedQueriesDiagrams.getId())
                .name(cannedQueriesDiagrams.getName())
                .description(cannedQueriesDiagrams.getDescription())
                .query(cannedQueriesDiagrams.getQuery())
                .build();
    }
}
