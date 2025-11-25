package backend.agent.WebSocketHandler;

import java.security.Principal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


@Controller
public class AgentController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


    public void sendToUser(String username, AgentMessageDTO message) {

    System.out.println("Sending message to " + username + " with correlationId " + message.getCorrelationId());
    simpMessagingTemplate.convertAndSendToUser(username, "/queue/reply", message);
    }

    @MessageMapping("/response")
    public void handleClientResponse(ClientResponseDTO response) {
        System.out.println("Received response for correlationId " + response.getCorrelationId() + ": " + response.getResult());

        // Optional: store response somewhere or notify a waiting thread
    }


}