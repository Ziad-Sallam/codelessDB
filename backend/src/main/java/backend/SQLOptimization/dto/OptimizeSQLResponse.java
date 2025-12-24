package backend.SQLOptimization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response object containing the optimized SQL and a summary of changes")
public class OptimizeSQLResponse {
    @Schema(description = "The optimized version of the provided SQL code", example = "SELECT id, name FROM users")
    private String optimizedSQL;

    @Schema(description = "A brief summary of the optimizations performed", example = "Removed unnecessary wildcard and selected specific columns.")
    private String summary;
}
