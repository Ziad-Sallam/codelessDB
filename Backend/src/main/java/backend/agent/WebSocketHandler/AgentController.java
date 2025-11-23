package backend.agent.WebSocketHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class AgentController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

@MessageMapping("/test")
public void send(AgentMessageDTO message) {  // <-- now expects MessageDTO
    simpMessagingTemplate.convertAndSend("/topic/messages", message);
    System.out.println("Server sent: " + message.getSender() + " says: " + message.getContent());
}


}