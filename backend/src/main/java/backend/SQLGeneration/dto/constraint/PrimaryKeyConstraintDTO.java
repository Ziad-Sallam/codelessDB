package backend.SQLGeneration.dto.constraint;

import backend.SQLGeneration.dto.ConstraintDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Represents a primary key constraint",
        example = "{ \"type\": \"PRIMARY_KEY\" }")
public class PrimaryKeyConstraintDTO implements ConstraintDTO {

    @Schema(
            description = "Constraint type",
            example = "PRIMARY_KEY",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private final String type = "PRIMARY_KEY";

    @Override
    public String toSQL() {
        return "PRIMARY KEY";
    }
}
