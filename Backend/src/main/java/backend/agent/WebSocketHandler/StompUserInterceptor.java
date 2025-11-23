package backend.agent.WebSocketHandler;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class StompUserInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = 
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            // Read STOMP header "Authorization"
            String username = accessor.getFirstNativeHeader("Authorization");

            if (username == null || username.isBlank()) {
                String x = "anon-" + UUID.randomUUID();
                accessor.setUser(() -> x);
                System.out.println("🔐 Connected user = " + x);
            }
            else{
                accessor.setUser(() -> username);
                System.out.println("🔐 Connected user = " + username);
            }

        }

        return message;
    }
}
