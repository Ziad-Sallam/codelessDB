package backend.SQLGeneration.dto.constraint;

import lombok.Getter;

@Getter
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
