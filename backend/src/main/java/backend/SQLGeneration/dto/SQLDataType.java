package backend.SQLGeneration.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SQLDataType {
    private SQLTypeName name;
    private Integer length;
    private Integer precision;
    private Integer scale;
}
