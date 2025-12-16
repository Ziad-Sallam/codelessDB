package backend.agent.HTTPHandler;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import backend.agent.WebSocketHandler.AgentController;
import backend.agent.WebSocketHandler.AgentMessageDTO;
import backend.agent.WebSocketHandler.ClientResponseDTO;
import backend.agent.WebSocketHandler.OnlineUserTracker;
import backend.databaseManagement.UserDatabaseRepository;
import backend.entities.User;
import backend.entities.UserDatabase;
import backend.user.exceptions.UserException.UserNotFoundException;
import backend.databaseManagement.exception.DatabaseException.DatabaseNotFoundException;
import backend.databaseManagement.exception.DatabaseException.DatabaseNotConnectedException;

import backend.user.UserRepository;
import lombok.RequiredArgsConstructor;

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
            throw new UserNotFoundException("User not found");

        UserDatabase database = userDatabaseRepository.findById(databaseId)
                .orElseThrow(() -> new DatabaseNotFoundException("Database not found"));

        if (!user.getAccessibleDatabases().contains(database))
            throw new AccessDeniedException("Unauthorized access");

        if (!tracker.isOnline(String.valueOf(databaseId)))
            throw new DatabaseNotConnectedException("Database not connected");

        try {
            clientResponse = agentController.sendToUser(databaseId, message);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return clientResponse;
    }

    public Boolean databaseIsOnline(int userId, int databaseId) {

        User user = userRepository.findById(userId);
        if (user == null)
            throw new UserNotFoundException("User not found");

        UserDatabase database = userDatabaseRepository.findById(databaseId)
                .orElseThrow(() -> new DatabaseNotFoundException("Database not found"));

        // Check accessibility before checking online
        if (!user.getAccessibleDatabases().contains(database))
            throw new AccessDeniedException("Unauthorized access");

        return tracker.isOnline(String.valueOf(databaseId));
    }

}
