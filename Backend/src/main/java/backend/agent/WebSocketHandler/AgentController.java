package backend.agent.WebSocketHandler;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


@Controller
public class AgentController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


    @MessageMapping("/test")
    public void send(Principal principal, AgentMessageDTO message) {

        String username = principal != null ? principal.getName() : "unknown";

        System.out.println("User " + username + " sent: " + message.getContent());

            // send ONLY to this user
        simpMessagingTemplate.convertAndSendToUser( username,                     // user
                                                    "/queue/reply",               // destination
                                                    message                       // payload
                                                );
    }

    public void sendToUser(String username, AgentMessageDTO message) {
        simpMessagingTemplate.convertAndSendToUser(username, "/queue/reply", message);
    }



}