package backend.SQLGeneration.dto.constraint;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Action applied on foreign key update/delete")
public enum ForeignKeyAction {
    CASCADE("CASCADE"),
    RESTRICT("RESTRICT"),
    SET_NULL("SET NULL"),
    SET_DEFAULT("SET DEFAULT"),
    NO_ACTION("NO ACTION");

    private final String sql;

    ForeignKeyAction(String sql) {
        this.sql = sql;
    }
}
