package backend.agent.WebSocketHandler;

import java.security.Principal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


@Controller
public class AgentController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    // Simple map to hold waiting requests
    private final ConcurrentHashMap<String, CompletableFuture<ClientResponseDTO>> pendingResponses = new ConcurrentHashMap<>();

    public ClientResponseDTO sendToUser(String username, AgentMessageDTO message) throws Exception {
        // Ensure correlationId exists
        if (message.getCorrelationId() == null) {
            message.setCorrelationId(UUID.randomUUID().toString());
        }
        String correlationId = message.getCorrelationId();

        // Create future and put it in map
        CompletableFuture<ClientResponseDTO> future = new CompletableFuture<>();
        pendingResponses.put(correlationId, future);

        // Send WebSocket message
        simpMessagingTemplate.convertAndSendToUser(username, "/queue/reply", message);

        try {
            // Wait for response (30 seconds timeout)
            return future.get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout waiting for client response: " + correlationId);
        } finally {
            // Cleanup map to prevent memory leak
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
