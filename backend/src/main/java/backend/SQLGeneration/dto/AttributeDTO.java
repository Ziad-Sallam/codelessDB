package backend.SQLGeneration.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDTO {
    private String name;
    private SQLDataType dataType;
    private boolean autoIncrement;
    private List<ConstraintDTO> constraints;
    private boolean indexed;
}
