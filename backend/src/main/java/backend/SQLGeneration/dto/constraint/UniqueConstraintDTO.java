package backend.SQLGeneration.dto.constraint;

import backend.SQLGeneration.dto.ConstraintDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Represents a unique constraint",
        example = "{ \"type\": \"UNIQUE\" }")
public class UniqueConstraintDTO implements ConstraintDTO {

    @Schema(
            description = "Constraint type",
            example = "UNIQUE",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private final String type = "UNIQUE";

    @Override
    public String toSQL() {
        return "UNIQUE";
    }
}
