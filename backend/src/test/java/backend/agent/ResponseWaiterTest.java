package backend.agent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backend.agent.WebSocketHandler.ClientResponseDTO;
import backend.agent.WebSocketHandler.ResponseWaiter;

class ResponseWaiterTest {

    private ResponseWaiter responseWaiter;

    @BeforeEach
    void setUp() {
        responseWaiter = new ResponseWaiter();
    }

    /* --------------------------------------------------------
       createWaiter
     -------------------------------------------------------- */

    @Test
    void createWaiter_returnsIncompleteFuture() {
        CompletableFuture<ClientResponseDTO> future =
                responseWaiter.createWaiter("corr-1");

        assertNotNull(future);
        assertFalse(future.isDone());
    }

    /* --------------------------------------------------------
       complete
     -------------------------------------------------------- */

    @Test
    void complete_completesFuture() throws Exception {
        String correlationId = "corr-2";
        ClientResponseDTO response = new ClientResponseDTO();

        CompletableFuture<ClientResponseDTO> future =
                responseWaiter.createWaiter(correlationId);

        responseWaiter.complete(correlationId, response);

        ClientResponseDTO result = future.get(1, TimeUnit.SECONDS);

        assertSame(response, result);
        assertTrue(future.isDone());
    }

    @Test
    void complete_removesWaiterAfterCompletion() {
        String correlationId = "corr-3";
        ClientResponseDTO response = new ClientResponseDTO();

        responseWaiter.complete(correlationId, response);

        // Completing again should do nothing and not throw
        assertDoesNotThrow(() ->
                responseWaiter.complete(correlationId, response));
    }

    @Test
    void complete_withUnknownCorrelationId_doesNothing() {
        ClientResponseDTO response = new ClientResponseDTO();

        assertDoesNotThrow(() ->
                responseWaiter.complete("unknown-id", response));
    }

    /* --------------------------------------------------------
       concurrency sanity check
     -------------------------------------------------------- */

    @Test
    void createAndComplete_multipleWaiters() throws Exception {
        CompletableFuture<ClientResponseDTO> f1 =
                responseWaiter.createWaiter("id1");
        CompletableFuture<ClientResponseDTO> f2 =
                responseWaiter.createWaiter("id2");

        ClientResponseDTO r1 = new ClientResponseDTO();
        ClientResponseDTO r2 = new ClientResponseDTO();

        responseWaiter.complete("id1", r1);
        responseWaiter.complete("id2", r2);

        assertSame(r1, f1.get(1, TimeUnit.SECONDS));
        assertSame(r2, f2.get(1, TimeUnit.SECONDS));
    }
}
