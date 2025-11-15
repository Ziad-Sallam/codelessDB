package backend.SQLGeneration.dto;
import lombok.Data;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemaDTO {
    private String schemaName;
    private List<EntityDTO> entities;
}