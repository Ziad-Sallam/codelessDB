package backend.agent.WebSocketHandler;

import java.util.concurrent.*;

import org.springframework.stereotype.Component;

@Component
public class ResponseWaiter {

    private final ConcurrentHashMap<String, CompletableFuture<ClientResponseDTO>> waiters =
            new ConcurrentHashMap<>();

    public CompletableFuture<ClientResponseDTO> createWaiter(String correlationId) {
        CompletableFuture<ClientResponseDTO> future = new CompletableFuture<>();
        waiters.put(correlationId, future);
        return future;
    }

    public void complete(String correlationId, ClientResponseDTO response) {
        CompletableFuture<ClientResponseDTO> future = waiters.remove(correlationId);
        if (future != null) {
            future.complete(response);
        }
    }
}

