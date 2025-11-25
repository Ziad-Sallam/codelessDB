package backend.agent.WebSocketHandler;

public class ClientResponseDTO {

    private String correlationId;
    private String result;
    private String sender;

    public ClientResponseDTO() {}

    public ClientResponseDTO(String correlationId, String result, String sender) {
        this.correlationId = correlationId;
        this.result = result;
        this.sender = sender;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return "ClientResponseDTO{" +
                "correlationId='" + correlationId + '\'' +
                ", result='" + result + '\'' +
                ", sender='" + sender + '\'' +
                '}';
    }
}
