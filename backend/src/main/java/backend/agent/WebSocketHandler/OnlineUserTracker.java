package backend.agent.WebSocketHandler;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class OnlineUserTracker {

    // Thread-safe set of usernames
    private final Set<String> onlineUsers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void addUser(String username) {
        onlineUsers.add(username);
        System.out.println("User added: " + username);
    }

    public void removeUser(String username) {
        onlineUsers.remove(username);
        System.out.println("User removed: " + username);
    }

    public boolean isOnline(String username) {
        return onlineUsers.contains(username);
    }

    public Set<String> getOnlineUsers() {
        return Collections.unmodifiableSet(onlineUsers);
    }
}
