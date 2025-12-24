package backend.SQLOptimization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request object for SQL optimization")
public class OptimizeSQLRequest {

    @NotBlank(message = "SQL code cannot be empty")
    @Schema(description = "The SQL code to be optimized", example = "SELECT * FROM users")
    private String sqlCode;
}
