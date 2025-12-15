package backend.agent.HTTPHandler;

import org.springframework.stereotype.Service;

import backend.agent.WebSocketHandler.AgentController;
import backend.agent.WebSocketHandler.AgentMessageDTO;
import backend.agent.WebSocketHandler.ClientResponseDTO;
import backend.agent.WebSocketHandler.OnlineUserTracker;
import backend.databaseManagement.UserDatabaseRepository;
import backend.entities.User;
import backend.entities.UserDatabase;
import backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final OnlineUserTracker tracker;
    private final AgentController agentController;
    private final UserDatabaseRepository userDatabaseRepository;
    private final UserRepository userRepository;

    public ClientResponseDTO runQuery(int databaseId, AgentMessageDTO message, int userId) {
        ClientResponseDTO clientResponse;
        User user = userRepository.findById(userId);
        if (user == null)
            throw new RuntimeException("User not found");
        if (!tracker.getOnlineUsers().contains(Integer.toString(databaseId)))
            throw new RuntimeException("Database not Connected Please Check your Server!");

        UserDatabase database = userDatabaseRepository.findById(databaseId).orElse(null);
        if (database == null)
            throw new RuntimeException("Database not found");

        if (!user.getAccessibleDatabases().contains(database))
            throw new RuntimeException("Unauthrized Access");

        try {
            clientResponse = agentController.sendToUser(databaseId, message);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return clientResponse;
    }

    public Boolean databaseIsOnline(int userId, int databaseId) {
        User user = userRepository.findById(userId);
        UserDatabase database = userDatabaseRepository.findById(databaseId).orElse(null);

        if (!user.getAccessibleDatabases().contains(database))
            throw new RuntimeException("Unauthrized Access");
        return tracker.isOnline(Integer.toString(databaseId));
    }

}
