package backend.SQLGeneration.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Represents an entity (table) in a database schema",
    example = """
    {
      "name": "users",
      "attributes": []
    }
    """
)
public class EntityDTO {
    @Schema(description = "Name of the entity", example = "users")
    private String name;

    @ArraySchema(
            arraySchema = @Schema(description = "List of attributes (columns) for this entity"),
            schema = @Schema(implementation = AttributeDTO.class)
    )
    private List<AttributeDTO> attributes;
}
