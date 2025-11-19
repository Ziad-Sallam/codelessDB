package backend.SQLGeneration.dto.constraint;

import backend.SQLGeneration.dto.ConstraintDTO;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForeignKeyConstraintDTO implements ConstraintDTO {
    private String referencedTable;
    private String referencedColumn;
    private ForeignKeyAction onDelete;
    private ForeignKeyAction onUpdate;

    @Override
    public String toSQL(String columnName) {
        return "FOREIGN KEY (" + columnName + ") REFERENCES " + referencedTable + "(" + referencedColumn + ")" +
                " ON DELETE " + onDelete.getSql() +
                " ON UPDATE " + onUpdate.getSql();
    }
}
