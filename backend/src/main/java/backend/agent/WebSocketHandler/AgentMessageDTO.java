package backend.agent.WebSocketHandler;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AgentMessageDTO {
    private String sender;
    private String content;
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
