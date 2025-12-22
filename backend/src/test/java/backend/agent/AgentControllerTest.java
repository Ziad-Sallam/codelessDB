package backend.agent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import backend.agent.WebSocketHandler.AgentController;
import backend.agent.WebSocketHandler.AgentMessageDTO;
import backend.agent.WebSocketHandler.ClientResponseDTO;

class AgentControllerTest {

    @InjectMocks
    private AgentController agentController;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /*
     * --------------------------------------------------------
     * sendToUser
     * --------------------------------------------------------
     */

    @Test
    void sendToUser_successfulResponse() throws Exception {
        AgentMessageDTO message = new AgentMessageDTO("agent", "run", "corr-1");

        int userId = 5;

        // Run sendToUser asynchronously (since it blocks on future.get)
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ClientResponseDTO> resultFuture = executor
                .submit(() -> agentController.sendToUser(userId, message));

        // Small delay to ensure sendToUser has registered the future
        Thread.sleep(100);

        ClientResponseDTO response = new ClientResponseDTO("corr-1", true, "OK");

        agentController.handleClientResponse(response);

        ClientResponseDTO result = resultFuture.get(1, TimeUnit.SECONDS);

        assertNotNull(result);
        assertEquals("corr-1", result.getCorrelationId());
        assertTrue(result.isSuccess());

        verify(simpMessagingTemplate, times(1))
                .convertAndSendToUser(
                        "5",
                        "/queue/reply",
                        message);

        executor.shutdownNow();
    }

    @Test
    void sendToUser_generatesCorrelationId_ifMissing() throws Exception {
        AgentMessageDTO message = new AgentMessageDTO("agent", "execute", null);

        int userId = 10;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ClientResponseDTO> future = executor.submit(() -> agentController.sendToUser(userId, message));

        Thread.sleep(100);

        assertNotNull(message.getCorrelationId());

        ClientResponseDTO response = new ClientResponseDTO(message.getCorrelationId(), true, "OK");

        agentController.handleClientResponse(response);

        ClientResponseDTO result = future.get(1, TimeUnit.SECONDS);

        assertEquals(message.getCorrelationId(), result.getCorrelationId());

        executor.shutdownNow();
    }

    @Test
    void sendToUser_timeoutThrowsException() {
        AgentMessageDTO message = new AgentMessageDTO("agent", "long-running", "timeout-id");

        int userId = 7;

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            agentController.sendToUser(userId, message);
        });

        assertTrue(ex.getMessage().contains("Timeout waiting for client response"));
    }

    /*
     * --------------------------------------------------------
     * handleClientResponse
     * --------------------------------------------------------
     */

    @Test
    void handleClientResponse_withNoWaitingRequest_doesNothing() {
        ClientResponseDTO response = new ClientResponseDTO("unknown-id", true, "OK");

        assertDoesNotThrow(() -> agentController.handleClientResponse(response));
    }
}
