package backend.SQLGeneration.dto.constraint;

import backend.SQLGeneration.dto.ConstraintDTO;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForeignKeyConstraintDTO implements ConstraintDTO {
    private String referencedTable;
    private String referencedColumn;
    private Action onDelete;
    private Action onUpdate;

    @Override
    public ConstraintType getType() {
        return ConstraintType.FOREIGN_KEY;
    }
}
