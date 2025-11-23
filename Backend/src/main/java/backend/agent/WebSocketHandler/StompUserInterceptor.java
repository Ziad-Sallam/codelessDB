package backend.agent.WebSocketHandler;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class StompUserInterceptor implements ChannelInterceptor {

    @Autowired
    private OnlineUserTracker tracker;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            String username = accessor.getFirstNativeHeader("Authorization");

            if (username == null || username.isBlank()) {

                username = "anon-" + UUID.randomUUID();
            }

            final String user = username;
            accessor.setUser(() -> user);

            tracker.addUser(user); // add to local server-side list
        }

        if (accessor != null && StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            if (accessor.getUser() != null) {
                String userName = accessor.getUser().getName();
                if (userName != null) {
                    tracker.removeUser(userName);
                }
            }
        }

        return message;
    }
}
