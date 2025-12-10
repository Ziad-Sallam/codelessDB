package backend.cannedquery;

import java.sql.Date;

import backend.entities.CannedQueriesDB;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CannedQueryDto {
    private Integer id;
    private String name;
    private String description;
    private String query;
    private Date createdAt;
    private Date updatedAt;
    private Integer databaseId;
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