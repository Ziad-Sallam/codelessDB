package backend.SQLGeneration.dto.constraint;

import backend.SQLGeneration.dto.ConstraintDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Represents a NOT NULL constraint",
        example = "{ \"type\": \"NOT_NULL\" }")
public class NotNullConstraintDTO implements ConstraintDTO {

    @Schema(
            description = "Constraint type",
            example = "NOT_NULL",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private final String type = "NOT_NULL";

    @Override
    public String toSQL() {
        return "NOT NULL";
    }
}
