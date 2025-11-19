package backend.SQLGeneration.dto;

import lombok.*;
import java.util.List;

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

