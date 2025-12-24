package backend.SQLGeneration.dto.constraint;

import backend.SQLGeneration.dto.ConstraintDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a DEFAULT constraint",
        example = "{ \"type\": \"DEFAULT\", \"defaultValue\": \"CURRENT_TIMESTAMP\" }")
public class DefaultConstraintDTO implements ConstraintDTO {

    @Schema(
            description = "Constraint type",
            example = "DEFAULT",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private final String type = "DEFAULT";

    @Schema(description = "The SQL expression for the default constraint", example = "DEFAULT")
    private String defaultValue;

    @Override
    public String toSQL() {
        return "DEFAULT " + defaultValue;
    }
}
