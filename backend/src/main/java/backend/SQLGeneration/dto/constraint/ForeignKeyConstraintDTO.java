package backend.SQLGeneration.dto.constraint;

import backend.SQLGeneration.dto.ConstraintDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a foreign key constraint")
public class ForeignKeyConstraintDTO implements ConstraintDTO {
    @Schema(description = "The name of the referenced table", example = "users")
    private String referencedTable;

    @Schema(description = "The name of the referenced column", example = "id")
    private String referencedColumn;

    @Schema(description = "The action to take on delete")
    private ForeignKeyAction onDelete;

    @Schema(description = "The action to take on update")
    private ForeignKeyAction onUpdate;

    @Override
    public String toSQL(String columnName) {
        return "FOREIGN KEY (" + columnName + ") REFERENCES " + referencedTable + "(" + referencedColumn + ")" +
                " ON DELETE " + onDelete.getSql() +
                " ON UPDATE " + onUpdate.getSql();
    }
}
