package backend.SQLGeneration.dto.constraint;

import backend.SQLGeneration.dto.ConstraintDTO;
import lombok.*;

@Data
@NoArgsConstructor
public class UniqueConstraintDTO implements ConstraintDTO {
    @Override
    public String toSQL() {
        return "UNIQUE";
    }
}
