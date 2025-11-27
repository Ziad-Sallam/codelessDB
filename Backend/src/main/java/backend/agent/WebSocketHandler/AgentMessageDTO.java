package backend.agent.WebSocketHandler;

import java.util.UUID;

public class AgentMessageDTO {
    private String sender;
    private String content;
    private String correlationId;

    public AgentMessageDTO() {}

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


    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}
