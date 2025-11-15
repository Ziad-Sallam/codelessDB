package backend.SQLGeneration.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDTO {
    private String name;
    private SQLDataType dataType;
    private boolean indexed;
    private List<ConstraintDTO> constraints;
}

