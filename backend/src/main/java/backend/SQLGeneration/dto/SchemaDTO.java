package backend.SQLGeneration.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemaDTO {
    private String schemaName;
    private List<EntityDTO> entities;
}