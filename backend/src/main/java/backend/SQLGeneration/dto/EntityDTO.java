package backend.SQLGeneration.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityDTO {
    private String name;
    private List<AttributeDTO> attributes;
}
