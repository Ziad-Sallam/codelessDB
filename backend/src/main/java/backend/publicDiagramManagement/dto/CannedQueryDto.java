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
public class CannedQueryDto {
    private String name;
    private String description;
    private String query;

    public static CannedQueryDto toDto(CannedQueriesDiagrams cannedQueriesDiagrams) {
        return CannedQueryDto.builder()
                .name(cannedQueriesDiagrams.getName())
                .description(cannedQueriesDiagrams.getDescription())
                .query(cannedQueriesDiagrams.getQuery())
                .build();
    }
}
