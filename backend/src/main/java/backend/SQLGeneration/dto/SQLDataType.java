package backend.SQLGeneration.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a SQL data type with optional modifiers like length and precision")
public class SQLDataType {
    @Schema(description = "The base name of the SQL data type", example = "VARCHAR")
    private SQLTypeName name;
    @Schema(description = "Length for character types (e.g. VARCHAR(255))", example = "255")
    private Integer length; // CHAR/VARCHAR
    @Schema(description = "Precision for numeric types (e.g. DECIMAL(10,2))", example = "10")
    private Integer precision; // DECIMAL/NUMERIC
    @Schema(description = "Scale for numeric types (e.g. DECIMAL(10,2))", example = "2")
    private Integer scale; // DECIMAL/NUMERIC
    @Schema(description = "List of allowed values for ENUM or SET types", example = "[\"ACTIVE\", \"INACTIVE\"]")
    private List<String> values; // ENUM/SET

    public String toDDL() {
        switch (name) {
            case VARCHAR, CHAR:
                return name + "(" + length + ")";
            case DECIMAL, NUMERIC:
                int p = precision != null ? precision : 10;
                int s = scale != null ? scale : 0;
                return name + "(" + p + "," + s + ")";
            case ENUM, SET:
                if (values == null || values.isEmpty())
                    return name + "()";
                String joined = String.join(",", values.stream()
                        .map(v -> "'" + v + "'")
                        .toArray(String[]::new));
                return name + "(" + joined + ")";
            default:
                return name.toString();
        }
    }
}
