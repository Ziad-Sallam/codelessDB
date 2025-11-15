package backend.SQLGeneration.dto.constraint;

import backend.SQLGeneration.dto.ConstraintDTO;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultConstraintDTO implements ConstraintDTO {
    private String defaultValue;


    @Override
    public ConstraintType getType() {
        return  ConstraintType.DEFAULT;
    }
}
