package backend.agent.HTTPHandler;

import backend.agent.WebSocketHandler.OnlineUserTracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import backend.agent.WebSocketHandler.AgentController;
import backend.agent.WebSocketHandler.AgentMessageDTO;
import backend.agent.WebSocketHandler.ClientResponseDTO;
import backend.databaseManagement.UserDatabaseRepository;
import backend.user.UserRepository;
import backend.entities.*;;

@Service
public class MessageService {
    private final OnlineUserTracker tracker;
    private final AgentController agentController;
    private final UserDatabaseRepository userDatabaseRepository;
    private final UserRepository userRepository;

    @Autowired
    public MessageService(
            UserRepository userRepository,
            UserDatabaseRepository userDatabaseRepository,
            OnlineUserTracker tracker,
            AgentController agentController
    ) {
        this.userRepository = userRepository;
        this.userDatabaseRepository = userDatabaseRepository;
        this.tracker = tracker;
        this.agentController = agentController;
    }

    public ClientResponseDTO runQuery(int databaseId, AgentMessageDTO message, int userId){
        ClientResponseDTO clientResponse;
        User user = userRepository.findById(userId);
        if (user == null) throw new RuntimeException("User not found");
        if(!tracker.getOnlineUsers().contains(Integer.toString(databaseId))) 
            throw new RuntimeException("Database not Connected Please Check your Server!");

        UserDatabase database = userDatabaseRepository.findById(databaseId).orElse(null);
        if(database == null) throw new RuntimeException("Database not found");
        
        if(!user.getAccessibleDatabases().contains(database))throw new RuntimeException("Unauthrized Access");

        try{
            clientResponse = agentController.sendToUser(databaseId, message);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }  
        return clientResponse;
    }
  
}
