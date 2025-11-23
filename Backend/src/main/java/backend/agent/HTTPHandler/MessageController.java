package backend.agent.HTTPHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import backend.agent.WebSocketHandler.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private AgentController agentController;

    @PostMapping("/send")
    public String sendToUser(@RequestBody MessageDTO request) {
        AgentMessageDTO message = new AgentMessageDTO("Server", request.getContent());
        agentController.sendToUser(request.getUsername(), message);
        return "Message sent to " + request.getUsername();
    }
}
