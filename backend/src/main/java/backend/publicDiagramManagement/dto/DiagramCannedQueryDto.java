package backend.publicDiagramManagement.dto;

import backend.entities.publicDiagramEntities.CannedQueriesDiagrams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for a canned query associated with a diagram")
public class DiagramCannedQueryDto {
    @Schema(description = "The name of the canned query", example = "Select all users")
    private String name;

    @Schema(description = "A description of the canned query", example = "Returns all rows from the users table")
    private String description;

    @Schema(description = "The SQL query string", example = "SELECT * FROM users")
    private String query;

    @Schema(description = "The unique identifier of the canned query mapping", example = "1")
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
