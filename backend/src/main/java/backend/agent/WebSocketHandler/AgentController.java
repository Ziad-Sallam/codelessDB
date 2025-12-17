package backend.agent.WebSocketHandler;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AgentController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ConcurrentHashMap<String, CompletableFuture<ClientResponseDTO>> pendingResponses = new ConcurrentHashMap<>();

    public ClientResponseDTO sendToUser(int username, AgentMessageDTO message) throws Exception {

        if (message.getCorrelationId() == null) {
            message.setCorrelationId(UUID.randomUUID().toString());
        }
        String correlationId = message.getCorrelationId();

        CompletableFuture<ClientResponseDTO> future = new CompletableFuture<>();
        pendingResponses.put(correlationId, future);

        simpMessagingTemplate.convertAndSendToUser(Integer.toString(username), "/queue/reply", message);

        try {

            return future.get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout waiting for client response: " + correlationId);
        } finally {

            pendingResponses.remove(correlationId);
        }
    }

    @MessageMapping("/response")
    public void handleClientResponse(ClientResponseDTO response) {
        String correlationId = response.getCorrelationId();
        CompletableFuture<ClientResponseDTO> future = pendingResponses.remove(correlationId);
        if (future != null) {
            future.complete(response);
        }
    }

}
