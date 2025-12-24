package backend.agent.WebSocketHandler;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Represents a message sent by or to the AI agent via WebSocket")
public class AgentMessageDTO {
    @Schema(description = "The sender of the message", example = "agent")
    private String sender;

    @Schema(description = "The content of the message", example = "The query has been executed successfully")
    private String content;

    @Schema(description = "The correlation ID for tracking request-response pairs", example = "550e8400-e29b-41d4-a716-446655440000")
    private String correlationId;

    public AgentMessageDTO() {
    }

    public AgentMessageDTO(String sender, String content, String correlationId) {
        this.sender = sender;
        this.content = content;
        this.correlationId = correlationId;
    }

    public AgentMessageDTO(String sender, String content) {
        this.sender = sender;
        this.content = content;
        String correlationId = UUID.randomUUID().toString();
        this.correlationId = correlationId;

    }
}
