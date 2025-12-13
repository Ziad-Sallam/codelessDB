package backend.agent.WebSocketHandler;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ClientResponseDTO {

    private String correlationId;
    private String type;
    private List<String> columns;
    private List<List<Object>> rows;
    private int rowCount;
    private boolean success;
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
