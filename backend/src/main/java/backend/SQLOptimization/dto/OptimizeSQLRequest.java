package backend.SQLOptimization.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OptimizeSQLRequest {
    
    @NotBlank(message = "SQL code cannot be empty")
    private String sqlCode;
}
