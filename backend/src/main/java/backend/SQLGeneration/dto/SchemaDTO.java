package backend.SQLGeneration.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a complete database schema structure")
public class SchemaDTO {
    @Schema(description = "Name of the database schema", example = "my_database")
    private String schemaName;

    @Schema(description = "List of entities (tables) in the schema", 
            example = "[{\"name\": \"users\", \"attributes\": [{\"name\": \"id\", \"dataType\": {\"name\": \"INT\"}, \"autoIncrement\": true, \"constraints\": [{\"type\": \"PRIMARY_KEY\"}]}]}]")
    private List<EntityDTO> entities;
}