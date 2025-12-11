package backend.SQLOptimization.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptimizeSQLResponse {
    private String optimizedSQL;
    private String summary;
}
