package backend.SQLGeneration.dto.constraint;

import backend.SQLGeneration.dto.ConstraintDTO;
import lombok.*;

@Data
@NoArgsConstructor
public class NotNullConstraintDTO implements ConstraintDTO {
    @Override
    public ConstraintType getType() {
        return ConstraintType.NOT_NULL;
    }
}
