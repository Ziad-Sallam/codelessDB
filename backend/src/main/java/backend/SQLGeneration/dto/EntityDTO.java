package backend.SQLGeneration.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents an entity (table) in a database schema")
public class EntityDTO {
    @Schema(description = "Name of the entity", example = "users")
    private String name;

    @Schema(description = "List of attributes (columns) for this entity")
    private List<AttributeDTO> attributes;
}
