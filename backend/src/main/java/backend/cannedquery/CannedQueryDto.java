package backend.cannedquery;

import java.time.LocalDateTime;

import backend.entities.CannedQueriesDB;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for represented a Canned Query")
public class CannedQueryDto {
    @Schema(description = "The unique identifier of the canned query", example = "1")
    private Integer id;

    @Schema(description = "The name of the canned query", example = "Get All Users")
    private String name;

    @Schema(description = "A description of what the query does", example = "Fetches all users from the database")
    private String description;

    @Schema(description = "The actual SQL query string", example = "SELECT * FROM users")
    private String query;

    @Schema(description = "The timestamp when the query was created")
    private LocalDateTime createdAt;

    @Schema(description = "The timestamp when the query was last updated")
    private LocalDateTime updatedAt;

    @Schema(description = "The ID of the database this query belongs to", example = "1")
    private Integer databaseId;

    @Schema(description = "The name of the database this query belongs to", example = "UserDB")
    private String databaseName;

    public CannedQueryDto(CannedQueriesDB entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.description = entity.getDescription();
        this.query = entity.getQuery();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
        this.databaseId = entity.getDatabase().getId();
        this.databaseName = entity.getDatabase().getName();
    }

    public CannedQueryDto(String name, String description, String query, Integer databaseId) {
        this.name = name;
        this.description = description;
        this.query = query;
        this.databaseId = databaseId;
    }
}