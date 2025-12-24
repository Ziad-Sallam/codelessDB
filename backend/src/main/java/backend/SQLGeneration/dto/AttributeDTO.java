package backend.SQLGeneration.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents an attribute (column) in a database table")
public class AttributeDTO {
    @Schema(description = "Name of the attribute", example = "id")
    private String name;

    @Schema(description = "Data type of the attribute")
    private SQLDataType dataType;

    @Schema(description = "Whether the attribute is auto-incrementing", example = "true")
    private boolean autoIncrement;

    @Schema(description = "List of constraints applied to this attribute", 
            example = "[{\"type\": \"PRIMARY_KEY\"}, {\"type\": \"NOT_NULL\"}]")
    private List<ConstraintDTO> constraints;

    @Schema(description = "Whether the attribute is indexed", example = "false")
    private boolean indexed;
}
