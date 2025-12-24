package backend.SQLGeneration.dto.constraint;

import backend.SQLGeneration.dto.ConstraintDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a check constraint with a custom SQL expression")
public class CheckConstraintDTO implements ConstraintDTO {
    @Schema(description = "The SQL expression for the check constraint", example = "age >= 18")
    private String expression;

    @Override
    public String toSQL() {
        return "CHECK (" + expression + ")";
    }
}