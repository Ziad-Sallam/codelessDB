package backend.SQLGeneration.dto.constraint;

import backend.SQLGeneration.dto.ConstraintDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Represents a unique constraint")
public class UniqueConstraintDTO implements ConstraintDTO {
    @Override
    public String toSQL() {
        return "UNIQUE";
    }
}
