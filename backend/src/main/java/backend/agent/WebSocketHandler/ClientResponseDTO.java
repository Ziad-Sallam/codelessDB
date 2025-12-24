package backend.agent.WebSocketHandler;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Represents the response from a client (like a database runner) back to the agent")
public class ClientResponseDTO {
    @Schema(description = "The correlation ID of the original request", example = "550e8400-e29b-41d4-a716-446655440000")
    private String correlationId;
    @Schema(description = "The type of response (e.g., SELECT, UPDATE, ERROR)", example = "SELECT")
    private String type;
    @Schema(description = "List of column names for SELECT queries")
    private List<String> columns;
    @Schema(description = "List of rows containing the query results")
    private List<List<Object>> rows;
    @Schema(description = "The number of rows affected or returned", example = "5")
    private int rowCount;
    @Schema(description = "Whether the operation was successful", example = "true")
    private boolean success;
    @Schema(description = "A message providing details about the operation's result", example = "Query executed successfully.")
    private String message;

    public ClientResponseDTO() {
    }

    // Constructor for SELECT responses
    public ClientResponseDTO(String correlationId,
            String type,
            List<String> columns,
            List<List<Object>> rows,
            int rowCount,
            boolean success) {
        this.correlationId = correlationId;
        this.type = type;
        this.columns = columns;
        this.rows = rows;
        this.rowCount = rowCount;
        this.success = success;
        this.message = "Query executed successfully.";
    }

    // Constructor for non-SELECT responses
    public ClientResponseDTO(String correlationId,
            boolean success,
            String type,
            int rowCount,
            String message) {
        this.correlationId = correlationId;
        this.success = success;
        this.type = type;
        this.rowCount = rowCount;
        this.message = message;
    }

    // Constructor for Error responses
    public ClientResponseDTO(String correlationId,
            boolean success,
            String message) {
        this.correlationId = correlationId;
        this.success = success;
        this.message = message;
        this.type = "ERROR";

    }

    @Override
    public String toString() {
        return "ClientResponseDTO{" +
                "correlationId='" + correlationId + '\n' +
                ", type='" + type + '\n' +
                ", columns=" + columns + '\n' +
                ", rows=" + rows + '\n' +
                ", rowCount=" + rowCount + '\n' +
                ", success=" + success + '\n' +
                ", message='" + message + '\n' +
                '}';
    }

}
