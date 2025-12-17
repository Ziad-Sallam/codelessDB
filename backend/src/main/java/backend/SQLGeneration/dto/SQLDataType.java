package backend.SQLGeneration.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SQLDataType {
    private SQLTypeName name;
    private Integer length; // CHAR/VARCHAR
    private Integer precision; // DECIMAL/NUMERIC
    private Integer scale; // DECIMAL/NUMERIC
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
