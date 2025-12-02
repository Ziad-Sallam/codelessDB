package backend.SQLGeneration.dto;

import backend.SQLGeneration.dto.constraint.*;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PrimaryKeyConstraintDTO.class, name = "PRIMARY_KEY"),
        @JsonSubTypes.Type(value = NotNullConstraintDTO.class, name = "NOT_NULL"),
        @JsonSubTypes.Type(value = ForeignKeyConstraintDTO.class, name = "FOREIGN_KEY"),
        @JsonSubTypes.Type(value = DefaultConstraintDTO.class, name = "DEFAULT"),
        @JsonSubTypes.Type(value = CheckConstraintDTO.class, name = "CHECK"),
        @JsonSubTypes.Type(value = UniqueConstraintDTO.class, name = "UNIQUE")
})
public interface ConstraintDTO {
    default String toSQL() { return ""; }
    default String toSQL(String columnName) { return toSQL(); }
}
