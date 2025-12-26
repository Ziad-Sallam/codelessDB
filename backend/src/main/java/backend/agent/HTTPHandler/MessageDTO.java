package backend.agent.HTTPHandler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@Schema(description = "Represents a message sent to the AI agent via HTTP")
public class MessageDTO {
    @Schema(description = "The ID of the database to perform operations on", example = "1")
    private int databaseId;

    @Schema(description = "The content of the message or query", example = "List all users")
    private String content;
}
