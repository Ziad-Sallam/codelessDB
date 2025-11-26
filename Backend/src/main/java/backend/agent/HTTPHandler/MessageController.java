package backend.agent.HTTPHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import backend.agent.WebSocketHandler.*;
import java.util.concurrent.TimeoutException;


@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private AgentController agentController;

    @PostMapping("/send")
    public ResponseEntity<?> sendToUser(@RequestBody MessageDTO request) {
        AgentMessageDTO message = new AgentMessageDTO("Server", request.getContent());

        try {
            // This will block until a client responds, up to the timeout
            ClientResponseDTO clientResponse = agentController.sendToUser(request.getUsername(), message);

            // Return the client response in HTTP body
            return ResponseEntity.ok(clientResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(e.getMessage());
        }
    }

}
