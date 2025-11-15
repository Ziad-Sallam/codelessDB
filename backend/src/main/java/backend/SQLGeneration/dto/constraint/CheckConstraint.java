package backend.SQLGeneration.dto.constraint;

import backend.SQLGeneration.dto.ConstraintDTO;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckConstraint implements ConstraintDTO {
    private String expression;

    @Override
    public ConstraintType getType() {
        return ConstraintType.CHECK;
    }
}