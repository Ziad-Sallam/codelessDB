package backend.SQLGeneration.dto.constraint;

import backend.SQLGeneration.dto.ConstraintDTO;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckConstraintDTO implements ConstraintDTO {
    private String expression;

    @Override
    public String toSQL() {
        return "CHECK (" + expression + ")";
    }
}